package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.*;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.mapper.*;
import com.trip.model.vo.FeedPageVO;
import com.trip.web.service.CommunityService;
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

    private final TripMapper tripMapper;
    private final TripUserMapper tripUserMapper;
    private final UserMapper userMapper;
    private final GraphInfoMapper graphInfoMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;

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
        Map<Long, Integer> commentCountMap =
                commentMapper.countByPostIds(postIds); // 你需要在 mapper.xml 写 group by


        /*
         * 8. 批量查 likeCount
         */
        Map<Long, Integer> likeCountMap =
                postLikeMapper.countByPostIds(postIds);


        /*
         * 9. 组装 VO
         */
        List<FeedPageVO.FeedItemVO> list = new ArrayList<>();

        for (Post post : records) {

            Long tripId = post.getTripId();
            Trip trip = tripMap.get(tripId);

            Long ownerId = tripOwnerMap.get(tripId);
            FeedPageVO.FeedItemVO.Author author = new FeedPageVO.FeedItemVO.Author();
            User user = userMap.get(ownerId);
            author.setUserId(user.getId());
            author.setNickName(user.getNickname());
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

            FeedPageVO.FeedItemVO.Stats stats = new FeedPageVO.FeedItemVO.Stats();
            stats.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            stats.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0));
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
}

