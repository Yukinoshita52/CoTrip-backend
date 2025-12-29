package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.TripVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 行程缓存服务
 * 使用Redis缓存用户行程列表，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "trip:user:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(30); // 30分钟过期

    /**
     * 生成缓存键
     */
    public String generateCacheKey(Long userId) {
        return CACHE_PREFIX + userId;
    }

    /**
     * 获取用户行程列表缓存
     */
    public List<TripVO> getUserTrips(Long userId) {
        String cacheKey = generateCacheKey(userId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("用户行程缓存命中: userId={}", userId);
                return objectMapper.readValue(cached, new TypeReference<List<TripVO>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("解析用户行程缓存失败: userId={}", userId, e);
        }
        return null;
    }

    /**
     * 缓存用户行程列表
     */
    public void cacheUserTrips(Long userId, List<TripVO> trips) {
        String cacheKey = generateCacheKey(userId);
        try {
            String jsonData = objectMapper.writeValueAsString(trips);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("用户行程已缓存: userId={}, count={}", userId, trips.size());
        } catch (JsonProcessingException e) {
            log.error("缓存用户行程失败: userId={}", userId, e);
        }
    }

    /**
     * 清除用户行程缓存
     */
    public void evictUserTrips(Long userId) {
        String cacheKey = generateCacheKey(userId);
        Boolean deleted = stringRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("已清除用户行程缓存: userId={}", userId);
        }
    }

    /**
     * 清除所有行程缓存
     */
    public void evictAllTrips() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有行程缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有行程缓存失败", e);
        }
    }

    /**
     * 当行程发生变化时，清除相关用户的缓存
     */
    public void evictTripRelatedCache(Long tripId, List<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            for (Long userId : userIds) {
                evictUserTrips(userId);
            }
            log.info("已清除行程相关用户缓存: tripId={}, userCount={}", tripId, userIds.size());
        }
    }
}