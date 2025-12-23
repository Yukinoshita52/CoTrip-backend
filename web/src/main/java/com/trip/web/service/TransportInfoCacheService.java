package com.trip.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 交通信息缓存服务
 * 缓存两点之间的交通信息（驾车、公交、步行）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransportInfoCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "transport_info:";
    // 缓存过期时间：30天（交通信息相对稳定，可以缓存较长时间）
    private static final long CACHE_EXPIRE_DAYS = 30;

    /**
     * 生成缓存键
     * 基于起点和终点的坐标以及交通方式生成唯一的缓存键
     * 
     * @param startLng 起点经度
     * @param startLat 起点纬度
     * @param endLng 终点经度
     * @param endLat 终点纬度
     * @param transportType 交通方式：driving, transit, walking
     * @return 缓存键
     */
    public String generateCacheKey(double startLng, double startLat, double endLng, double endLat, String transportType) {
        try {
            // 将坐标和交通方式转换为字符串（保留4位小数精度，减少缓存键数量）
            String input = String.format("%.4f,%.4f;%.4f,%.4f;%s", 
                startLng, startLat, endLng, endLat, transportType);
            
            // 使用MD5哈希
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hash = new StringBuilder();
            for (byte b : digest) {
                hash.append(String.format("%02x", b));
            }
            return CACHE_PREFIX + transportType + ":" + hash.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成交通信息缓存键失败", e);
            return CACHE_PREFIX + transportType + ":" + 
                (startLng + startLat + endLng + endLat + transportType).hashCode();
        }
    }

    /**
     * 从缓存中获取交通信息
     * 
     * @param cacheKey 缓存键
     * @return 交通信息（包含distance和duration），如果缓存未命中返回null
     */
    public Map<String, Object> getTransportInfo(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(cached, Map.class);
                return result;
            }
            return null;
        } catch (Exception e) {
            log.error("从缓存获取交通信息失败: key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 将交通信息存入缓存
     * 
     * @param cacheKey 缓存键
     * @param transportData 交通信息（包含distance和duration）
     */
    public void cacheTransportInfo(String cacheKey, Map<String, Object> transportData) {
        try {
            if (transportData != null && !transportData.isEmpty()) {
                // 验证必要字段
                if (transportData.containsKey("distance") && transportData.containsKey("duration")) {
                    // 序列化为JSON字符串并存入缓存，设置30天过期时间
                    String jsonData = objectMapper.writeValueAsString(transportData);
                    stringRedisTemplate.opsForValue().set(
                        cacheKey, 
                        jsonData, 
                        CACHE_EXPIRE_DAYS, 
                        TimeUnit.DAYS
                    );
                } else {
                    log.warn("交通信息数据缺少必要字段，跳过缓存: key={}", cacheKey);
                }
            }
        } catch (Exception e) {
            log.error("缓存交通信息失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除特定的交通信息缓存
     * 
     * @param cacheKey 缓存键
     */
    public void evictTransportInfo(String cacheKey) {
        try {
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("已清除交通信息缓存: key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("清除交通信息缓存失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除所有交通信息缓存
     */
    public void evictAllTransportInfo() {
        try {
            var keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("已清除所有交通信息缓存: 共{}个", deleted);
            }
        } catch (Exception e) {
            log.error("清除所有交通信息缓存失败", e);
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
            return String.format("交通信息缓存统计: 共%d个缓存项", count);
        } catch (Exception e) {
            log.error("获取交通信息缓存统计信息失败", e);
            return "获取缓存统计信息失败: " + e.getMessage();
        }
    }
}

