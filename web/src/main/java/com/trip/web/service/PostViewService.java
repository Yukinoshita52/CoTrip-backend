package com.trip.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 帖子浏览量服务
 * 使用Redis实现浏览量计数
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PostViewService {

    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String VIEW_COUNT_PREFIX = "post:view:";
    private static final String USER_VIEW_PREFIX = "post:user_view:";

    /**
     * 增加帖子浏览量
     * @param postId 帖子ID
     * @param userId 用户ID（可选，用于防止重复计数）
     * @return 当前浏览量
     */
    public Long incrementViewCount(Long postId, Long userId) {
        String viewCountKey = VIEW_COUNT_PREFIX + postId;
        
        // 如果提供了用户ID，检查该用户是否已经浏览过
        if (userId != null) {
            String userViewKey = USER_VIEW_PREFIX + postId + ":" + userId;
            Boolean hasViewed = stringRedisTemplate.hasKey(userViewKey);
            
            if (Boolean.TRUE.equals(hasViewed)) {
                // 用户已经浏览过，不增加计数，直接返回当前计数
                String count = stringRedisTemplate.opsForValue().get(viewCountKey);
                return count != null ? Long.valueOf(count) : 0L;
            }
            
            // 标记用户已浏览，设置24小时过期（防止同一用户短时间内重复计数）
            stringRedisTemplate.opsForValue().set(userViewKey, "1", java.time.Duration.ofHours(24));
        }
        
        // 增加浏览计数
        Long newCount = stringRedisTemplate.opsForValue().increment(viewCountKey);
        log.info("帖子浏览量增加: postId={}, newCount={}", postId, newCount);
        
        return newCount;
    }

    /**
     * 获取帖子浏览量
     * @param postId 帖子ID
     * @return 浏览量
     */
    public Long getViewCount(Long postId) {
        String viewCountKey = VIEW_COUNT_PREFIX + postId;
        String count = stringRedisTemplate.opsForValue().get(viewCountKey);
        return count != null ? Long.valueOf(count) : 0L;
    }

    /**
     * 设置帖子浏览量（用于初始化或同步数据库数据）
     * @param postId 帖子ID
     * @param count 浏览量
     */
    public void setViewCount(Long postId, Long count) {
        String viewCountKey = VIEW_COUNT_PREFIX + postId;
        stringRedisTemplate.opsForValue().set(viewCountKey, String.valueOf(count));
        log.info("设置帖子浏览量: postId={}, count={}", postId, count);
    }

    /**
     * 批量获取多个帖子的浏览量
     * @param postIds 帖子ID列表
     * @return 帖子ID到浏览量的映射
     */
    public java.util.Map<Long, Long> getViewCounts(java.util.List<Long> postIds) {
        java.util.Map<Long, Long> result = new java.util.HashMap<>();
        
        for (Long postId : postIds) {
            Long count = getViewCount(postId);
            result.put(postId, count);
        }
        
        return result;
    }

    /**
     * 清除所有浏览量缓存（用于测试或维护）
     */
    public void clearAllViewCounts() {
        try {
            var keys = stringRedisTemplate.keys(VIEW_COUNT_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("已清除所有浏览量缓存: 共{}个", keys.size());
            }
        } catch (Exception e) {
            log.error("清除浏览量缓存失败", e);
        }
    }
}