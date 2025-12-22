package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.web.service.BaiduRouteCacheService;
import com.trip.web.service.LLMCacheService;
import com.trip.web.service.RoutePlanCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * LLM缓存管理控制器
 * 提供所有LLM相关缓存的管理和监控功能
 */
@RestController
@RequestMapping("/api/admin/llm-cache")
@RequiredArgsConstructor
@Slf4j
public class LLMCacheController {

    private final LLMCacheService llmCacheService;
    private final RoutePlanCacheService routePlanCacheService;
    private final BaiduRouteCacheService baiduRouteCacheService;

    /**
     * 获取百度地图路线规划缓存统计信息
     */
    @GetMapping("/baidu-route/stats")
    public Result<String> getBaiduRouteCacheStats() {
        try {
            String stats = baiduRouteCacheService.getCacheStats();
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取百度地图路线规划缓存统计信息失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有百度地图路线规划缓存
     */
    @DeleteMapping("/baidu-route/all")
    public Result<String> clearAllBaiduRouteCache() {
        try {
            baiduRouteCacheService.evictAllRouteCache();
            return Result.ok("已清除所有百度地图路线规划缓存");
        } catch (Exception e) {
            log.error("清除所有百度地图路线规划缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除所有缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取路径规划缓存统计信息
     */
    @GetMapping("/route-plan/stats")
    public Result<String> getRoutePlanCacheStats() {
        try {
            String stats = routePlanCacheService.getCacheStats();
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取路径规划缓存统计信息失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有路径规划缓存
     */
    @DeleteMapping("/route-plan/all")
    public Result<String> clearAllRoutePlanCache() {
        try {
            routePlanCacheService.evictAllRoutePlans();
            return Result.ok("已清除所有路径规划缓存");
        } catch (Exception e) {
            log.error("清除所有路径规划缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除所有缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除地点导入相关的LLM缓存
     */
    @DeleteMapping("/place-import/all")
    public Result<String> clearPlaceImportCache() {
        try {
            llmCacheService.evictLLMCacheByPrefix("place_import");
            return Result.ok("已清除所有地点导入缓存");
        } catch (Exception e) {
            log.error("清除地点导入缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除地点导入缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除地点类型判断相关的LLM缓存
     */
    @DeleteMapping("/place-type/all")
    public Result<String> clearPlaceTypeCache() {
        try {
            llmCacheService.evictLLMCacheByPrefix("place_type");
            return Result.ok("已清除所有地点类型判断缓存");
        } catch (Exception e) {
            log.error("清除地点类型判断缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除地点类型判断缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有LLM缓存
     */
    @DeleteMapping("/all")
    public Result<String> clearAllLLMCache() {
        try {
            // 清除路径规划缓存
            routePlanCacheService.evictAllRoutePlans();
            
            // 清除百度地图路线规划缓存
            baiduRouteCacheService.evictAllRouteCache();
            
            // 清除地点导入缓存
            llmCacheService.evictLLMCacheByPrefix("place_import");
            
            // 清除地点类型判断缓存
            llmCacheService.evictLLMCacheByPrefix("place_type");
            
            return Result.ok("已清除所有LLM相关缓存");
        } catch (Exception e) {
            log.error("清除所有LLM缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除所有LLM缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除特定的LLM缓存
     */
    @DeleteMapping("/{cacheKey}")
    public Result<String> clearSpecificCache(@PathVariable String cacheKey) {
        try {
            llmCacheService.evictLLMCache(cacheKey);
            return Result.ok("已清除缓存: " + cacheKey);
        } catch (Exception e) {
            log.error("清除缓存失败: {}", cacheKey, e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(),"清除缓存失败: " + e.getMessage());
        }
    }
}
