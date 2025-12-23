package com.trip.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 路径规划缓存服务
 * 使用Redis缓存路径规划结果，减少LLM API调用
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoutePlanCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "route_plan:";
    
    /**
     * 生成缓存键
     * 基于地点信息和行程天数生成唯一的缓存键
     * 
     * @param placesInfo 地点信息字符串
     * @param tripDays 行程天数
     * @return 缓存键
     */
    public String generateCacheKey(String placesInfo, int tripDays) {
        try {
            // 使用地点信息和天数生成MD5哈希作为缓存键
            String input = placesInfo + "_days:" + tripDays;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return CACHE_PREFIX + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成缓存键失败", e);
            // 如果MD5失败，使用简单的哈希码
            return CACHE_PREFIX + (placesInfo + "_days:" + tripDays).hashCode();
        }
    }

    /**
     * 从缓存中获取路径规划结果
     * 
     * @param cacheKey 缓存键
     * @return 路径规划结果JSON字符串，如果缓存未命中返回null
     */
    public String getRoutePlan(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("路径规划缓存命中: key={}", cacheKey);
                return cached;
            }
            log.debug("路径规划缓存未命中: key={}", cacheKey);
            return null;
        } catch (Exception e) {
            log.error("从缓存获取路径规划失败: key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 将路径规划结果存入缓存
     * 
     * @param cacheKey 缓存键
     * @param routePlanJson 路径规划结果JSON字符串
     */
    public void cacheRoutePlan(String cacheKey, String routePlanJson) {
        try {
            // 验证JSON格式
            objectMapper.readTree(routePlanJson);
            
            // 存入缓存，设置永不过期
            stringRedisTemplate.opsForValue().set(cacheKey, routePlanJson);
            log.info("路径规划结果已缓存: key={}, size={} bytes", cacheKey, routePlanJson.length());
        } catch (JsonProcessingException e) {
            log.error("缓存路径规划失败，JSON格式无效: key={}, json={}", cacheKey, routePlanJson, e);
        } catch (Exception e) {
            log.error("缓存路径规划失败: key={}", cacheKey, e);
        }
    }

    /**
     * 验证缓存的路径规划结果是否有效
     * 
     * @param routePlanJson 路径规划JSON字符串
     * @param expectedPlaceIds 期望的地点ID列表
     * @return 是否有效
     */
    public boolean isValidRoutePlan(String routePlanJson, List<Long> expectedPlaceIds) {
        try {
            JsonNode node = objectMapper.readTree(routePlanJson);
            if (!node.isArray()) {
                log.warn("缓存的路径规划格式无效：不是数组格式");
                return false;
            }

            // 检查是否包含所有期望的地点
            for (Long expectedPlaceId : expectedPlaceIds) {
                boolean found = false;
                for (JsonNode item : node) {
                    if (item.path("placeId").asLong() == expectedPlaceId) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.warn("缓存的路径规划缺少地点: placeId={}", expectedPlaceId);
                    return false;
                }
            }

            log.debug("缓存的路径规划验证通过");
            return true;
        } catch (Exception e) {
            log.error("验证缓存路径规划失败", e);
            return false;
        }
    }

    /**
     * 清除特定的路径规划缓存
     * 
     * @param cacheKey 缓存键
     */
    public void evictRoutePlan(String cacheKey) {
        try {
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("已清除路径规划缓存: key={}", cacheKey);
            } else {
                log.debug("路径规划缓存不存在或清除失败: key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("清除路径规划缓存失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除所有路径规划缓存
     */
    public void evictAllRoutePlans() {
        try {
            // 获取所有匹配的键
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有路径规划缓存: 共{}个", deleted);
            } else {
                log.info("没有找到路径规划缓存");
            }
        } catch (Exception e) {
            log.error("清除所有路径规划缓存失败", e);
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
            return String.format("路径规划缓存统计: 共%d个缓存项", count);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return "获取缓存统计信息失败: " + e.getMessage();
        }
    }
}