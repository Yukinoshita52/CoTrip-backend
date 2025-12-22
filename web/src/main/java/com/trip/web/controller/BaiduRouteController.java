package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.web.service.BaiduRouteCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 百度地图路线规划缓存控制器
 */
@RestController
@RequestMapping("/api/baidu-route")
@RequiredArgsConstructor
@Slf4j
public class BaiduRouteController {

    private final BaiduRouteCacheService baiduRouteCacheService;

    /**
     * 检查路线规划缓存
     * 
     * @param places 地点列表
     * @return 缓存的路线规划结果，如果没有缓存返回null
     */
    @PostMapping("/cache/check")
    public Result<Map<String, Object>> checkRouteCache(@RequestBody List<Map<String, Object>> places) {
        try {
            if (places == null || places.size() < 2) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"至少需要2个地点");
            }

            String cacheKey = baiduRouteCacheService.generateCacheKey(places);
            Map<String, Object> cachedRoute = baiduRouteCacheService.getRouteCache(cacheKey);
            
            if (cachedRoute != null) {
                return Result.ok(cachedRoute);
            } else {
                return Result.ok(null); // 缓存未命中
            }
        } catch (Exception e) {
            log.error("检查路线规划缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"检查缓存失败: " + e.getMessage());
        }
    }

    /**
     * 保存路线规划结果到缓存
     * 
     * @param request 包含地点列表和路线规划结果的请求
     * @return 操作结果
     */
    @PostMapping("/cache/save")
    public Result<String> saveRouteCache(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> places = (List<Map<String, Object>>) request.get("places");
            @SuppressWarnings("unchecked")
            Map<String, Object> routeData = (Map<String, Object>) request.get("routeData");

            if (places == null || places.size() < 2) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"至少需要2个地点");
            }

            if (routeData == null || routeData.isEmpty()) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"路线规划结果不能为空");
            }

            String cacheKey = baiduRouteCacheService.generateCacheKey(places);
            baiduRouteCacheService.cacheRoute(cacheKey, routeData);
            
            return Result.ok("路线规划结果已缓存");
        } catch (Exception e) {
            log.error("保存路线规划缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"保存缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public Result<String> getCacheStats() {
        try {
            String stats = baiduRouteCacheService.getCacheStats();
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有路线规划缓存
     */
    @DeleteMapping("/cache/all")
    public Result<String> clearAllCache() {
        try {
            baiduRouteCacheService.evictAllRouteCache();
            return Result.ok("已清除所有百度地图路线规划缓存");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除所有缓存失败: " + e.getMessage());
        }
    }
}