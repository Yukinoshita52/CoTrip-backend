package com.trip.web.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis缓存清理测试
 * 用于清理可能导致序列化问题的旧缓存数据
 */
@Slf4j
@SpringBootTest
public class RedisCacheCleanupTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 清理所有可能有问题的缓存数据
     */
    @Test
    public void cleanupAllCache() {
        log.info("=== 开始清理Redis缓存数据 ===");
        
        try {
            // 清理浏览量缓存
            cleanupCacheByPattern("post:view:*");
            cleanupCacheByPattern("post:user_view:*");
            
            // 清理LLM缓存
            cleanupCacheByPattern("llm:*");
            cleanupCacheByPattern("place_type:*");
            cleanupCacheByPattern("batch_import:*");
            
            // 清理路径规划缓存
            cleanupCacheByPattern("route_plan:*");
            
            // 清理百度地图缓存
            cleanupCacheByPattern("baidu_route:*");
            
            log.info("=== Redis缓存清理完成 ===");
        } catch (Exception e) {
            log.error("清理Redis缓存失败", e);
        }
    }

    /**
     * 清理指定模式的缓存
     */
    private void cleanupCacheByPattern(String pattern) {
        try {
            var keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = stringRedisTemplate.delete(keys);
                log.info("清理缓存: 模式={}, 删除数量={}", pattern, deleted);
            } else {
                log.info("清理缓存: 模式={}, 无数据需要清理", pattern);
            }
        } catch (Exception e) {
            log.error("清理缓存失败: 模式={}", pattern, e);
        }
    }

    /**
     * 测试Redis连接
     */
    @Test
    public void testRedisConnection() {
        log.info("=== 测试Redis连接 ===");
        
        try {
            // 测试基本操作
            String testKey = "test:connection";
            String testValue = "test_value_" + System.currentTimeMillis();
            
            // 设置值
            stringRedisTemplate.opsForValue().set(testKey, testValue);
            log.info("设置测试值: key={}, value={}", testKey, testValue);
            
            // 获取值
            String retrievedValue = stringRedisTemplate.opsForValue().get(testKey);
            log.info("获取测试值: key={}, value={}", testKey, retrievedValue);
            
            // 删除测试键
            Boolean deleted = stringRedisTemplate.delete(testKey);
            log.info("删除测试键: key={}, deleted={}", testKey, deleted);
            
            if (testValue.equals(retrievedValue)) {
                log.info("Redis连接测试成功");
            } else {
                log.error("Redis连接测试失败: 值不匹配");
            }
            
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
        }
        
        log.info("=== Redis连接测试完成 ===");
    }

    /**
     * 初始化一些测试数据
     */
    @Test
    public void initializeTestData() {
        log.info("=== 初始化测试数据 ===");
        
        try {
            // 初始化一些浏览量数据
            stringRedisTemplate.opsForValue().set("post:view:1", "100");
            stringRedisTemplate.opsForValue().set("post:view:2", "50");
            stringRedisTemplate.opsForValue().set("post:view:3", "200");
            
            log.info("初始化浏览量数据完成");
            
            // 验证数据
            String view1 = stringRedisTemplate.opsForValue().get("post:view:1");
            String view2 = stringRedisTemplate.opsForValue().get("post:view:2");
            String view3 = stringRedisTemplate.opsForValue().get("post:view:3");
            
            log.info("验证数据: post:1={}, post:2={}, post:3={}", view1, view2, view3);
            
        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
        }
        
        log.info("=== 测试数据初始化完成 ===");
    }
}