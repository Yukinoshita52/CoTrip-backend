package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 百度地图路线规划缓存服务
 * 缓存前端百度地图API的路线规划结果
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BaiduRouteCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "baidu_route:";

    /**
     * 生成缓存键
     * 基于地点坐标列表生成唯一的缓存键
     * 
     * @param places 地点列表，每个地点包含lng和lat
     * @return 缓存键
     */
    public String generateCacheKey(List<Map<String, Object>> places) {
        try {
            // 将地点列表转换为字符串
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> place : places) {
                sb.append(place.get("lng")).append(",").append(place.get("lat")).append(";");
            }
            String input = sb.toString();
            
            // 使用MD5哈希
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hash = new StringBuilder();
            for (byte b : digest) {
                hash.append(String.format("%02x", b));
            }
            return CACHE_PREFIX + hash.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成缓存键失败", e);
            return CACHE_PREFIX + places.hashCode();
        }
    }

    /**
     * 从缓存中获取路线规划结果
     * 
     * @param cacheKey 缓存键
     * @return 路线规划结果，如果缓存未命中返回null
     */
    public Map<String, Object> getRouteCache(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("百度地图路线规划缓存命中: key={}", cacheKey);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(cached, Map.class);
                return result;
            }
            log.debug("百度地图路线规划缓存未命中: key={}", cacheKey);
            return null;
        } catch (Exception e) {
            log.error("从缓存获取路线规划失败: key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 将路线规划结果存入缓存
     * 
     * @param cacheKey 缓存键
     * @param routeData 路线规划结果（包含绘制数据）
     */
    public void cacheRoute(String cacheKey, Map<String, Object> routeData) {
        try {
            if (routeData != null && !routeData.isEmpty()) {
                // 验证必要字段
                if (routeData.containsKey("distance") && routeData.containsKey("duration")) {
                    // 序列化为JSON字符串并存入缓存，设置永不过期
                    String jsonData = objectMapper.writeValueAsString(routeData);
                    stringRedisTemplate.opsForValue().set(cacheKey, jsonData);
                    
                    // 记录缓存的数据类型
                    boolean hasRoutePolyline = routeData.containsKey("routePolyline") && 
                        routeData.get("routePolyline") != null;
                    
                    log.info("百度地图路线规划结果已缓存: key={}, 包含绘制数据={}", 
                        cacheKey, hasRoutePolyline);
                } else {
                    log.warn("路线规划数据缺少必要字段，跳过缓存: key={}", cacheKey);
                }
            }
        } catch (Exception e) {
            log.error("缓存路线规划失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除特定的路线规划缓存
     * 
     * @param cacheKey 缓存键
     */
    public void evictRouteCache(String cacheKey) {
        try {
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("已清除百度地图路线规划缓存: key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("清除路线规划缓存失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除所有百度地图路线规划缓存
     */
    public void evictAllRouteCache() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有百度地图路线规划缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有百度地图路线规划缓存失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            int count = keys != null ? keys.size() : 0;
            return String.format("百度地图路线规划缓存统计: 共%d个缓存项", count);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return "获取缓存统计信息失败: " + e.getMessage();
        }
    }
}
