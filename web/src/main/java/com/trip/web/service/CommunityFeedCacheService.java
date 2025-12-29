package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.FeedPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 社区动态缓存服务
 * 使用Redis缓存社区动态列表，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityFeedCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "community:feed:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(5); // 5分钟过期，社区内容更新较频繁

    /**
     * 生成缓存键
     */
    public String generateCacheKey(Integer page, Integer size) {
        return CACHE_PREFIX + page + ":" + size;
    }

    /**
     * 获取社区动态缓存
     */
    public FeedPageVO getFeed(Integer page, Integer size) {
        String cacheKey = generateCacheKey(page, size);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("社区动态缓存命中: page={}, size={}", page, size);
                return objectMapper.readValue(cached, FeedPageVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析社区动态缓存失败: page={}, size={}", page, size, e);
        }
        return null;
    }

    /**
     * 缓存社区动态
     */
    public void cacheFeed(Integer page, Integer size, FeedPageVO feedPageVO) {
        String cacheKey = generateCacheKey(page, size);
        try {
            String jsonData = objectMapper.writeValueAsString(feedPageVO);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("社区动态已缓存: page={}, size={}, count={}", page, size, feedPageVO.getList().size());
        } catch (JsonProcessingException e) {
            log.error("缓存社区动态失败: page={}, size={}", page, size, e);
        }
    }

    /**
     * 清除社区动态缓存
     */
    public void evictFeed(Integer page, Integer size) {
        String cacheKey = generateCacheKey(page, size);
        Boolean deleted = stringRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("已清除社区动态缓存: page={}, size={}", page, size);
        }
    }

    /**
     * 清除所有社区动态缓存
     */
    public void evictAllFeeds() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有社区动态缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有社区动态缓存失败", e);
        }
    }

    /**
     * 当有新帖子或帖子更新时，清除相关缓存
     */
    public void evictFeedOnPostChange() {
        evictAllFeeds();
        log.info("帖子变化，已清除所有社区动态缓存");
    }
}