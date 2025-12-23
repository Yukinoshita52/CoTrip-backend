package com.trip.web.service;

import com.trip.model.vo.PostLikeVO;
import java.util.List;
import java.util.Map;

/**
 * 帖子点赞服务接口
 * 基于Redis实现高性能点赞系统
 */
public interface PostLikeService {
    
    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 点赞结果
     */
    PostLikeVO likePost(Long postId, Long userId);
    
    /**
     * 取消点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 取消点赞结果
     */
    PostLikeVO unlikePost(Long postId, Long userId);
    
    /**
     * 检查用户是否已点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isPostLikedByUser(Long postId, Long userId);
    
    /**
     * 获取帖子点赞总数
     * @param postId 帖子ID
     * @return 点赞总数
     */
    Long getPostLikeCount(Long postId);
    
    /**
     * 批量获取多个帖子的点赞数
     * @param postIds 帖子ID列表
     * @return 帖子ID到点赞数的映射
     */
    Map<Long, Long> getPostLikeCounts(List<Long> postIds);
    
    /**
     * 批量检查用户对多个帖子的点赞状态
     * @param postIds 帖子ID列表
     * @param userId 用户ID
     * @return 帖子ID到点赞状态的映射
     */
    Map<Long, Boolean> getUserLikeStatuses(List<Long> postIds, Long userId);
    
    /**
     * 从MySQL同步点赞数据到Redis（用于初始化或数据恢复）
     * @param postId 帖子ID，如果为null则同步所有帖子
     */
    void syncLikeDataFromMySQL(Long postId);
    
    /**
     * 将Redis中的点赞数据同步到MySQL（定期同步任务）
     */
    void syncLikeDataToMySQL();
    
    /**
     * 清除Redis中的点赞缓存（用于测试或维护）
     */
    void clearLikeCache();
}