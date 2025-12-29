package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.CommentListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 评论缓存服务
 * 使用Redis缓存帖子评论，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "comments:post:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(10); // 10分钟过期

    /**
     * 生成缓存键
     */
    public String generateCacheKey(Long postId) {
        return CACHE_PREFIX + postId;
    }

    /**
     * 获取评论列表缓存
     */
    public CommentListVO getComments(Long postId) {
        String cacheKey = generateCacheKey(postId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("评论列表缓存命中: postId={}", postId);
                return objectMapper.readValue(cached, CommentListVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析评论列表缓存失败: postId={}", postId, e);
        }
        return null;
    }

    /**
     * 缓存评论列表
     */
    public void cacheComments(Long postId, CommentListVO comments) {
        String cacheKey = generateCacheKey(postId);
        try {
            String jsonData = objectMapper.writeValueAsString(comments);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("评论列表已缓存: postId={}, count={}", postId, 
                comments.getComments() != null ? comments.getComments().size() : 0);
        } catch (JsonProcessingException e) {
            log.error("缓存评论列表失败: postId={}", postId, e);
        }
    }

    /**
     * 清除评论列表缓存
     */
    public void evictComments(Long postId) {
        String cacheKey = generateCacheKey(postId);
        Boolean deleted = stringRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("已清除评论列表缓存: postId={}", postId);
        }
    }

    /**
     * 清除所有评论缓存
     */
    public void evictAllComments() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有评论缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有评论缓存失败", e);
        }
    }

    /**
     * 当有新评论或评论变化时，清除相关缓存
     */
    public void evictCommentsOnChange(Long postId) {
        evictComments(postId);
        log.info("评论变化，已清除相关缓存: postId={}", postId);
    }
}