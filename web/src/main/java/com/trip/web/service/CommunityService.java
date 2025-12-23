package com.trip.web.service;

import com.trip.model.dto.TripDTO;
import com.trip.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.*;

import java.util.List;

/**
 * ClassName: CommunityService
 * Package: com.trip.web.service
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 17:34
 * @Version 1.0
 */
public interface CommunityService extends IService<Post> {
    FeedPageVO getFeed(Integer page, Integer size);

    PostDetailVO getPostDetail(Long postId);

    StatVO getPostStats(Long postId, Long userId);

    /**
     * 根据tripId创建帖子（也就是用户分享了自己的行程到社区）
     * @param dto
     * @return
     */
    PostCreatedVO createPost(Long userId,TripDTO dto);

    /**
     *
     * 根据userId获取用户的信息、以及用户所发布的文章信息
     * @param userId
     * @return
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 获取当前用户已分享的行程ID列表
     * @param userId 用户ID
     * @return 已分享的行程ID列表
     */
    List<Long> getUserSharedTripIds(Long userId);

    SearchPostVO searchMatchPosts(String keyword);

    SearchUserVO searchAuthorByKeyword(String keyword);
}