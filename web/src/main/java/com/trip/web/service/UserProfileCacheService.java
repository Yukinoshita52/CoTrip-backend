package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 用户资料缓存服务
 * 使用Redis缓存用户资料信息，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "user:profile:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(20); // 20分钟过期

    /**
     * 生成缓存键
     */
    public String generateCacheKey(Long userId) {
        return CACHE_PREFIX + userId;
    }

    /**
     * 获取用户资料缓存
     */
    public UserProfileVO getUserProfile(Long userId) {
        String cacheKey = generateCacheKey(userId);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("用户资料缓存命中: userId={}", userId);
                return objectMapper.readValue(cached, UserProfileVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析用户资料缓存失败: userId={}", userId, e);
        }
        return null;
    }

    /**
     * 缓存用户资料
     */
    public void cacheUserProfile(Long userId, UserProfileVO userProfile) {
        String cacheKey = generateCacheKey(userId);
        try {
            String jsonData = objectMapper.writeValueAsString(userProfile);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("用户资料已缓存: userId={}", userId);
        } catch (JsonProcessingException e) {
            log.error("缓存用户资料失败: userId={}", userId, e);
        }
    }

    /**
     * 清除用户资料缓存
     */
    public void evictUserProfile(Long userId) {
        String cacheKey = generateCacheKey(userId);
        Boolean deleted = stringRedisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("已清除用户资料缓存: userId={}", userId);
        }
    }

    /**
     * 清除所有用户资料缓存
     */
    public void evictAllUserProfiles() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有用户资料缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有用户资料缓存失败", e);
        }
    }
}