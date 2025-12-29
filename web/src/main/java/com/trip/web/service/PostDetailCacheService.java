package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.PostDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 帖子详情缓存服务
 * 使用Redis缓存帖子详情，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostDetailCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "post:detail:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(15); // 15分钟过期

    /**
     * 生成缓存键
     */
    public String generateCacheKey(Long postId) {
        return CACHE_PREFIX + postId;
    }

    /**
     * 获取帖子详情缓存
     */
    public PostDetailVO getPostDetail(Long postId) {
        String cacheKey = generateCacheKey(postId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("帖子详情缓存命中: postId={}", postId);
                return objectMapper.readValue(cached, PostDetailVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析帖子详情缓存失败: postId={}", postId, e);
        }
        return null;
    }

    /**
     * 缓存帖子详情
     */
    public void cachePostDetail(Long postId, PostDetailVO postDetail) {
        String cacheKey = generateCacheKey(postId);
        try {
            String jsonData = objectMapper.writeValueAsString(postDetail);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("帖子详情已缓存: postId={}", postId);
        } catch (JsonProcessingException e) {
            log.error("缓存帖子详情失败: postId={}", postId, e);
        }
    }

    /**
     * 清除帖子详情缓存
     */
    public void evictPostDetail(Long postId) {
        String cacheKey = generateCacheKey(postId);
        Boolean deleted = stringRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("已清除帖子详情缓存: postId={}", postId);
        }
    }

    /**
     * 清除所有帖子详情缓存
     */
    public void evictAllPostDetails() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有帖子详情缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有帖子详情缓存失败", e);
        }
    }

    /**
     * 当帖子相关数据变化时，清除相关缓存
     */
    public void evictPostRelatedCache(Long postId) {
        evictPostDetail(postId);
        log.info("已清除帖子相关缓存: postId={}", postId);
    }
}