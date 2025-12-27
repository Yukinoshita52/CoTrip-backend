package com.trip.web.service;

import com.trip.model.vo.PostCollectVO;

/**
 * 帖子收藏服务接口
 */
public interface PostCollectService {
    
    /**
     * 收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 收藏结果
     */
    PostCollectVO collectPost(Long postId, Long userId);
    
    /**
     * 取消收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 取消收藏结果
     */
    PostCollectVO uncollectPost(Long postId, Long userId);
    
    /**
     * 检查用户是否已收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    Boolean isPostCollectedByUser(Long postId, Long userId);
    
    /**
     * 获取帖子收藏数
     * @param postId 帖子ID
     * @return 收藏数
     */
    Long getPostCollectCount(Long postId);
}