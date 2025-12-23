package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.dto.CommentCountDTO;
import com.trip.model.dto.LikeCountDTO;
import com.trip.model.dto.TripDTO;
import com.trip.model.entity.*;
import com.trip.model.vo.*;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.mapper.*;
import com.trip.web.service.CommunityService;
import com.trip.web.service.PostViewService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: CommunityServiceImpl
 * Package: com.trip.web.service.impl
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 20:44
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl extends ServiceImpl<PostMapper, Post> implements CommunityService {

    @Resource
    private final TripMapper tripMapper;
    @Resource
    private final TripUserMapper tripUserMapper;
    @Resource
    private final UserMapper userMapper;
    @Resource
    private final GraphInfoMapper graphInfoMapper;
    @Resource
    private final CommentMapper commentMapper;
    @Resource
    private final PostLikeMapper postLikeMapper;
    @Resource
    private final PlaceMapper placeMapper;
    @Resource
    private final PostMapper postMapper;
    @Resource
    private final CommunityMapper communityMapper;
    @Resource
    private final PostViewService postViewService;

    /**
     * 1.首先对post表是分页查询数据的，查询到一系列的id as post_id。
     * 2.根据这个post_id，关联查询trip表，得到id as trip_id、name as trip_name、region、start_date、end_date、description
     * 3.根据关系trip_user关系表，查询行程trip对应的user表中的用户，然后从user表中查询nickname。
     * 4.从graph_info中，需要查询两部分信息：
     *  4.1根据user表的id（user_id）查询用户头像的url（通过item_type = 1 and item_id = user_id进行过滤）
     *  4.2根据trip表的id（trip_Id）查询行程图片的url（通过item_type = 2 ans item_id = trip_id进行过滤）
     * 5.根据trip_id，查询comment表中count(*)的记录数 as commentCount
     * 6.根据trip_id，查询post_like表中count(*)的记录数 as likeCount
     * @param page
     * @param size
     * @return
     */
    @Override
    public FeedPageVO getFeed(Integer page, Integer size) {

        Page<Post> mpPage = new Page<>(page, size);

        // 1. 分页查 post
        Page<Post> postPage = this.page(
                mpPage,
                new LambdaQueryWrapper<Post>()
                        .orderByDesc(Post::getCreateTime)
        );

        List<Post> records = postPage.getRecords();
        if (records.isEmpty()) {
            FeedPageVO empty = new FeedPageVO();
            empty.setPage(page);
            empty.setSize(size);
            empty.setTotal(0L);
            empty.setList(Collections.emptyList());
            return empty;
        }

        // 收集 id
        List<Long> postIds = records.stream().map(Post::getId).collect(Collectors.toList());
        List<Long> tripIds = records.stream().map(Post::getTripId).collect(Collectors.toList());

        /*
         * 2. 批量查 trip
         */
        Map<Long, Trip> tripMap = tripMapper.selectBatchIds(tripIds)
                .stream().collect(Collectors.toMap(Trip::getId, t -> t));


        /*
         * 3. 批量查 trip_user 中的创建者（role = 0）
         */
        List<TripUser> tripUsers = tripUserMapper.selectList(
                new LambdaQueryWrapper<TripUser>()
                        .in(TripUser::getTripId, tripIds)
                        .eq(TripUser::getRole, 0)
        );

        Map<Long, Long> tripOwnerMap = tripUsers.stream()
                .collect(Collectors.toMap(TripUser::getTripId, TripUser::getUserId));


        /*
         * 4. 批量查 user 信息
         */
        List<Long> userIds = tripOwnerMap.values().stream().distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));


        /*
         * 5. 批量查用户头像（item_type = 1）
         */
        Map<Long, String> avatarMap = graphInfoMapper.selectList(
                        new LambdaQueryWrapper<GraphInfo>()
                                .eq(GraphInfo::getItemType, 1)
                                .in(GraphInfo::getItemId, userIds)
                ).stream()
                .collect(Collectors.toMap(GraphInfo::getItemId, GraphInfo::getUrl));


        /*
         * 6. 批量查 Trip 封面图（item_type = 2）
         */
        Map<Long, List<String>> coverMap = graphInfoMapper.selectList(
                        new LambdaQueryWrapper<GraphInfo>()
                                .eq(GraphInfo::getItemType, 2)
                                .in(GraphInfo::getItemId, tripIds)
                ).stream()
                .collect(Collectors.groupingBy(
                        GraphInfo::getItemId,
                        Collectors.mapping(GraphInfo::getUrl, Collectors.toList())
                ));


        /*
         * 7. 批量查 commentCount
         */
        Map<Long, CommentCountDTO> commentCountMap =
                commentMapper.countByPostIds(postIds);


        /*
         * 8. 批量查 likeCount
         */
        Map<Long, LikeCountDTO> likeCountMap =
                postLikeMapper.countByPostIds(postIds);


        /*
         * 9. 组装 VO
         */
        List<FeedPageVO.FeedItemVO> list = new ArrayList<>();

        for (Post post : records) {

            Long tripId = post.getTripId();
            Trip trip = tripMap.get(tripId);

            Long ownerId = tripOwnerMap.get(tripId);
            AuthorVO author = new AuthorVO();
            User user = userMap.get(ownerId);
            author.setUserId(user.getId());
            author.setNickname(user.getNickname());
            author.setAvatar(avatarMap.get(ownerId));

            FeedPageVO.FeedItemVO vo = new FeedPageVO.FeedItemVO();
            vo.setPostId(post.getId());
            vo.setTripId(trip.getId());
            vo.setTripName(trip.getName());
            vo.setRegion(trip.getRegion());
            vo.setStartDate(trip.getStartDate());
            vo.setEndDate(trip.getEndDate());
            vo.setDescription(trip.getDescription());
            vo.setCoverImages(coverMap.getOrDefault(tripId, Collections.emptyList()));
            vo.setAuthor(author);

            StatVO stats = new StatVO();
            CommentCountDTO commentCountDTO = commentCountMap.getOrDefault(post.getId(), null);
            LikeCountDTO likeCountDTO = likeCountMap.getOrDefault(post.getId(), null);
            stats.setCommentCount(commentCountDTO == null ? 0 : commentCountDTO.getCommentCount());
            stats.setLikeCount(likeCountDTO == null ? 0 : likeCountDTO.getLikeCount());
            // 从Redis获取浏览量
            Long viewCount = postViewService.getViewCount(post.getId());
            stats.setViewCount(viewCount.intValue());
            vo.setStats(stats);

            vo.setCreateTime(post.getCreateTime());

            list.add(vo);
        }

        FeedPageVO result = new FeedPageVO();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(postPage.getTotal());
        result.setList(list);

        return result;
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        //数据查询
        Post post = postMapper.selectById(postId);
        if(post == null){
            return null;
        }
        Trip trip = tripMapper.selectById(post.getTripId());//从trip中获取一些数据
        List<PlaceDayTypeVO> days = placeMapper.getPlaceDayTypeByTripId(trip.getId());
        List<String> images = graphInfoMapper.getTripImagesByTripId(trip.getId());
        AuthorVO author = tripUserMapper.getAuthorByTripId(trip.getId());
        StatVO stats = postMapper.getStatsByPostId(post.getId(),author.getUserId());

        //数据封装
        PostDetailVO vo = new PostDetailVO();
        vo.setPostId(postId);
        PostDetailVO.TripDetailVO tripDetailVO = new PostDetailVO.TripDetailVO();
        tripDetailVO.setTripId(trip.getId());
        tripDetailVO.setName(trip.getName());
        tripDetailVO.setRegion(trip.getRegion());
        tripDetailVO.setStartDate(trip.getStartDate());
        tripDetailVO.setEndDate(trip.getEndDate());
        tripDetailVO.setDescription(trip.getDescription());
        tripDetailVO.setDays(days);
        tripDetailVO.setImages(images);
        vo.setTrip(tripDetailVO);
        vo.setAuthor(author);
        vo.setStats(stats);
        vo.setCreateTime(post.getCreateTime());

        return vo;
    }

    @Override
    public PostCreatedVO createPost(Long userId,TripDTO dto) {
//        如果dto中的行程已经被删除了，那么这里就不能再进行创建了
        if (tripMapper.selectById(dto.getTripId()) == null) {
            return null;
        }


        Post post = new Post();
        post.setUserId(userId);
        post.setTripId(dto.getTripId());
        postMapper.insert(post);

        PostCreatedVO res = new PostCreatedVO();
        res.setPostId(post.getId());
        res.setCreateTime(post.getCreateTime());
        res.setTripId(dto.getTripId());
        return res;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        AuthorVO authorVO = userMapper.getAuthorVoByUserId(userId);
        UserPostsStatsVO stats = userMapper.getUserPostStatsByUserId(userId);
        List<UserPostVO> posts = userMapper.getUserPostsByUserId(userId);

        UserProfileVO res = new UserProfileVO();
        res.setUserId(userId);
        res.setNickname(authorVO.getNickname());
        res.setAvatar(authorVO.getAvatar());
        res.setStats(stats);
        res.setPosts(posts);
        return res;
    }

    @Override
    public SearchPostVO searchMatchPosts(String keyword) {
        List<SearchPostVO.SearchItemVO> items = communityMapper.getPostsByKeyWord(keyword);

        SearchPostVO res = new SearchPostVO();
        res.setResults(items);
        res.setKeyword(keyword);
        return res;
    }

    @Override
    public SearchUserVO searchAuthorByKeyword(String keyword) {
        List<AuthorVO> users = userMapper.getAuthorVoByKeyword(keyword);

        SearchUserVO res = new SearchUserVO();
        res.setKeyword(keyword);
        res.setUsers(users);
        return res;
    }
}

