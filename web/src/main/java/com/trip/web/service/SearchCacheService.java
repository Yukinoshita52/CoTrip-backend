package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.model.vo.SearchPostVO;
import com.trip.model.vo.SearchUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 搜索缓存服务
 * 使用Redis缓存搜索结果，提高响应速度
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String POST_SEARCH_PREFIX = "search:post:";
    private static final String USER_SEARCH_PREFIX = "search:user:";
    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(5); // 5分钟过期，搜索结果变化较快

    /**
     * 生成帖子搜索缓存键
     */
    public String generatePostSearchCacheKey(String keyword) {
        return POST_SEARCH_PREFIX + keyword.toLowerCase().trim();
    }

    /**
     * 生成用户搜索缓存键
     */
    public String generateUserSearchCacheKey(String keyword) {
        return USER_SEARCH_PREFIX + keyword.toLowerCase().trim();
    }

    /**
     * 获取帖子搜索结果缓存
     */
    public SearchPostVO getPostSearchResult(String keyword) {
        String cacheKey = generatePostSearchCacheKey(keyword);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("帖子搜索缓存命中: keyword={}", keyword);
                return objectMapper.readValue(cached, SearchPostVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析帖子搜索缓存失败: keyword={}", keyword, e);
        }
        return null;
    }

    /**
     * 缓存帖子搜索结果
     */
    public void cachePostSearchResult(String keyword, SearchPostVO searchResult) {
        String cacheKey = generatePostSearchCacheKey(keyword);
        try {
            String jsonData = objectMapper.writeValueAsString(searchResult);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("帖子搜索结果已缓存: keyword={}, count={}", keyword, searchResult.getResults().size());
        } catch (JsonProcessingException e) {
            log.error("缓存帖子搜索结果失败: keyword={}", keyword, e);
        }
    }

    /**
     * 获取用户搜索结果缓存
     */
    public SearchUserVO getUserSearchResult(String keyword) {
        String cacheKey = generateUserSearchCacheKey(keyword);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("用户搜索缓存命中: keyword={}", keyword);
                return objectMapper.readValue(cached, SearchUserVO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析用户搜索缓存失败: keyword={}", keyword, e);
        }
        return null;
    }

    /**
     * 缓存用户搜索结果
     */
    public void cacheUserSearchResult(String keyword, SearchUserVO searchResult) {
        String cacheKey = generateUserSearchCacheKey(keyword);
        try {
            String jsonData = objectMapper.writeValueAsString(searchResult);
            stringRedisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_EXPIRATION);
            log.info("用户搜索结果已缓存: keyword={}, count={}", keyword, searchResult.getUsers().size());
        } catch (JsonProcessingException e) {
            log.error("缓存用户搜索结果失败: keyword={}", keyword, e);
        }
    }

    /**
     * 清除所有搜索缓存
     */
    public void evictAllSearchCache() {
        try {
            var postKeys = stringRedisTemplate.keys(POST_SEARCH_PREFIX + "*");
            var userKeys = stringRedisTemplate.keys(USER_SEARCH_PREFIX + "*");
            
            int deletedCount = 0;
            if (postKeys != null && !postKeys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(postKeys);
                deletedCount += deleted != null ? deleted.intValue() : 0;
            }
            if (userKeys != null && !userKeys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(userKeys);
                deletedCount += deleted != null ? deleted.intValue() : 0;
            }
            
            log.info("已清除所有搜索缓存: 共{}个", deletedCount);
        } catch (Exception e) {
            log.error("清除所有搜索缓存失败", e);
        }
    }

    /**
     * 当有新帖子或用户信息变化时，清除搜索缓存
     */
    public void evictSearchCacheOnDataChange() {
        evictAllSearchCache();
        log.info("数据变化，已清除所有搜索缓存");
    }
}