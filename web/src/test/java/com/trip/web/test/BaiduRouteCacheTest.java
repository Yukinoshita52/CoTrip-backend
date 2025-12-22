package com.trip.web.test;

import com.trip.web.service.BaiduRouteCacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度地图路线规划缓存测试
 */
@SpringBootTest
@Slf4j
public class BaiduRouteCacheTest {

    @Autowired
    private BaiduRouteCacheService baiduRouteCacheService;

    @Test
    public void testBaiduRouteCache() {
        // 准备测试数据
        List<Map<String, Object>> places = new ArrayList<>();
        
        Map<String, Object> place1 = new HashMap<>();
        place1.put("lng", 116.404);
        place1.put("lat", 39.915);
        place1.put("name", "天安门");
        places.add(place1);
        
        Map<String, Object> place2 = new HashMap<>();
        place2.put("lng", 116.407);
        place2.put("lat", 39.904);
        place2.put("name", "故宫");
        places.add(place2);

        // 生成缓存键
        String cacheKey = baiduRouteCacheService.generateCacheKey(places);
        log.info("生成的缓存键: {}", cacheKey);

        // 测试缓存未命中
        Map<String, Object> cachedRoute = baiduRouteCacheService.getRouteCache(cacheKey);
        log.info("首次查询缓存结果: {}", cachedRoute);

        // 准备完整的路线数据（包含绘制数据）
        Map<String, Object> routeData = new HashMap<>();
        routeData.put("distance", 1500);
        routeData.put("duration", 300);
        routeData.put("toll", 0);
        routeData.put("timestamp", System.currentTimeMillis());
        
        // 模拟路线绘制数据
        List<Map<String, Object>> routePolyline = new ArrayList<>();
        Map<String, Object> point1 = new HashMap<>();
        point1.put("lng", 116.404);
        point1.put("lat", 39.915);
        routePolyline.add(point1);
        
        Map<String, Object> point2 = new HashMap<>();
        point2.put("lng", 116.405);
        point2.put("lat", 39.910);
        routePolyline.add(point2);
        
        Map<String, Object> point3 = new HashMap<>();
        point3.put("lng", 116.407);
        point3.put("lat", 39.904);
        routePolyline.add(point3);
        
        routeData.put("routePolyline", routePolyline);

        // 保存到缓存
        baiduRouteCacheService.cacheRoute(cacheKey, routeData);
        log.info("完整路线数据已保存到缓存");

        // 测试缓存命中
        Map<String, Object> cachedRoute2 = baiduRouteCacheService.getRouteCache(cacheKey);
        log.info("第二次查询缓存结果: {}", cachedRoute2);
        
        // 验证绘制数据
        if (cachedRoute2 != null && cachedRoute2.containsKey("routePolyline")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cachedPolyline = (List<Map<String, Object>>) cachedRoute2.get("routePolyline");
            log.info("缓存中的路线绘制数据点数量: {}", cachedPolyline.size());
        }

        // 获取缓存统计
        String stats = baiduRouteCacheService.getCacheStats();
        log.info("缓存统计: {}", stats);
    }

    @Test
    public void testClearCache() {
        // 清除所有缓存
        baiduRouteCacheService.evictAllRouteCache();
        log.info("已清除所有百度地图路线规划缓存");

        // 获取缓存统计
        String stats = baiduRouteCacheService.getCacheStats();
        log.info("清除后缓存统计: {}", stats);
    }
}