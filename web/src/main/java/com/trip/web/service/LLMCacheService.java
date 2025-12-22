package com.trip.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * LLM通用缓存服务
 * 为所有LLM调用提供缓存功能，减少API调用次数
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LLMCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成缓存键
     * 
     * @param prefix 缓存前缀，用于区分不同类型的LLM调用
     * @param prompt LLM提示词
     * @return 缓存键
     */
    public String generateCacheKey(String prefix, String prompt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(prompt.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return prefix + ":" + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成缓存键失败", e);
            return prefix + ":" + prompt.hashCode();
        }
    }

    /**
     * 从缓存中获取LLM响应
     * 
     * @param cacheKey 缓存键
     * @return LLM响应，如果缓存未命中返回null
     */
    public String getLLMResponse(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("LLM缓存命中: key={}", cacheKey);
                return cached.toString();
            }
            log.debug("LLM缓存未命中: key={}", cacheKey);
            return null;
        } catch (Exception e) {
            log.error("从缓存获取LLM响应失败: key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 将LLM响应存入缓存
     * 
     * @param cacheKey 缓存键
     * @param response LLM响应
     */
    public void cacheLLMResponse(String cacheKey, String response) {
        try {
            if (response != null && !response.trim().isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, response);
                log.info("LLM响应已缓存: key={}, size={} bytes", cacheKey, response.length());
            }
        } catch (Exception e) {
            log.error("缓存LLM响应失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除特定的LLM缓存
     * 
     * @param cacheKey 缓存键
     */
    public void evictLLMCache(String cacheKey) {
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("已清除LLM缓存: key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("清除LLM缓存失败: key={}", cacheKey, e);
        }
    }

    /**
     * 清除指定前缀的所有LLM缓存
     * 
     * @param prefix 缓存前缀
     */
    public void evictLLMCacheByPrefix(String prefix) {
        try {
            var keys = redisTemplate.keys(prefix + ":*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("已清除LLM缓存: 前缀={}, 数量={}", prefix, deleted);
            }
        } catch (Exception e) {
            log.error("清除LLM缓存失败: prefix={}", prefix, e);
        }
    }
}