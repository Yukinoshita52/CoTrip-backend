package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.TransportInfoRequest;
import com.trip.model.vo.TransportInfoVO;
import com.trip.web.service.TransportInfoCacheService;
import com.trip.web.service.TransportInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交通信息缓存控制器
 */
@RestController
@RequestMapping("/api/transport-info")
@RequiredArgsConstructor
@Slf4j
public class TransportInfoController {

    private final TransportInfoCacheService transportInfoCacheService;
    private final TransportInfoService transportInfoService;

    /**
     * 查询交通信息（完整流程：检查缓存 -> 调用百度地图API -> 保存缓存）
     * 
     * @param request 包含起点、终点坐标和交通方式的请求
     * @return 交通信息
     */
    @PostMapping("/query")
    public Result<TransportInfoVO> queryTransportInfo(@RequestBody TransportInfoRequest request) {
        try {
            if (request.getStartLng() == null || request.getStartLat() == null || 
                request.getEndLng() == null || request.getEndLat() == null) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "起点和终点坐标不能为空");
            }

            if (request.getTransportType() == null || request.getTransportType().isEmpty()) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式不能为空");
            }

            if (!request.getTransportType().equals("driving") && 
                !request.getTransportType().equals("transit") && 
                !request.getTransportType().equals("walking")) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式必须是driving、transit或walking");
            }

            TransportInfoVO result = transportInfoService.queryTransportInfo(
                request.getStartLng(), request.getStartLat(),
                request.getEndLng(), request.getEndLat(),
                request.getTransportType()
            ).block(); // 同步等待结果

            if (result != null) {
                return Result.ok(result);
            } else {
                return Result.fail(ResultCodeEnum.FAIL.getCode(), "获取交通信息失败");
            }
        } catch (Exception e) {
            log.error("查询交通信息失败", e);
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "查询交通信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查交通信息缓存（保留用于兼容）
     * 
     * @param request 包含起点、终点坐标和交通方式的请求
     * @return 缓存的交通信息，如果没有缓存返回null
     */
    @PostMapping("/cache/check")
    public Result<Map<String, Object>> checkTransportInfoCache(@RequestBody Map<String, Object> request) {
        try {
            Double startLng = getDoubleValue(request.get("startLng"));
            Double startLat = getDoubleValue(request.get("startLat"));
            Double endLng = getDoubleValue(request.get("endLng"));
            Double endLat = getDoubleValue(request.get("endLat"));
            String transportType = (String) request.get("transportType");

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "起点和终点坐标不能为空");
            }

            if (transportType == null || transportType.isEmpty()) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式不能为空");
            }

            if (!transportType.equals("driving") && !transportType.equals("transit") && !transportType.equals("walking")) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式必须是driving、transit或walking");
            }

            String cacheKey = transportInfoCacheService.generateCacheKey(
                startLng, startLat, endLng, endLat, transportType);
            Map<String, Object> cachedInfo = transportInfoCacheService.getTransportInfo(cacheKey);
            
            if (cachedInfo != null) {
                return Result.ok(cachedInfo);
            } else {
                return Result.ok(null); // 缓存未命中
            }
        } catch (Exception e) {
            log.error("检查交通信息缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "检查缓存失败: " + e.getMessage());
        }
    }

    /**
     * 保存交通信息到缓存
     * 
     * @param request 包含起点、终点坐标、交通方式和交通信息的请求
     * @return 操作结果
     */
    @PostMapping("/cache/save")
    public Result<String> saveTransportInfoCache(@RequestBody Map<String, Object> request) {
        try {
            Double startLng = getDoubleValue(request.get("startLng"));
            Double startLat = getDoubleValue(request.get("startLat"));
            Double endLng = getDoubleValue(request.get("endLng"));
            Double endLat = getDoubleValue(request.get("endLat"));
            String transportType = (String) request.get("transportType");
            @SuppressWarnings("unchecked")
            Map<String, Object> transportData = (Map<String, Object>) request.get("transportData");

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "起点和终点坐标不能为空");
            }

            if (transportType == null || transportType.isEmpty()) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式不能为空");
            }

            if (!transportType.equals("driving") && !transportType.equals("transit") && !transportType.equals("walking")) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通方式必须是driving、transit或walking");
            }

            if (transportData == null || transportData.isEmpty()) {
                return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "交通信息不能为空");
            }

            String cacheKey = transportInfoCacheService.generateCacheKey(
                startLng, startLat, endLng, endLat, transportType);
            transportInfoCacheService.cacheTransportInfo(cacheKey, transportData);
            
            return Result.ok("交通信息已缓存");
        } catch (Exception e) {
            log.error("保存交通信息缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "保存缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public Result<String> getCacheStats() {
        try {
            String stats = transportInfoCacheService.getCacheStats();
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有交通信息缓存
     */
    @DeleteMapping("/cache/all")
    public Result<String> clearAllCache() {
        try {
            transportInfoCacheService.evictAllTransportInfo();
            return Result.ok("已清除所有交通信息缓存");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            return Result.fail(ResultCodeEnum.CACHE_EXCEPTION.getCode(), "清除所有缓存失败: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：将Object转换为Double
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

