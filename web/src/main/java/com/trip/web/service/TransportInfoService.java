package com.trip.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.trip.model.vo.TransportInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 交通信息服务
 * 负责调用百度地图API并管理缓存
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransportInfoService {

    private final BaiduMapService baiduMapService;
    private final TransportInfoCacheService transportInfoCacheService;

    /**
     * 查询交通信息（带缓存）
     * 
     * @param startLng 起点经度
     * @param startLat 起点纬度
     * @param endLng 终点经度
     * @param endLat 终点纬度
     * @param transportType 交通方式：driving, transit, walking
     * @return 交通信息
     */
    public Mono<TransportInfoVO> queryTransportInfo(double startLng, double startLat, 
                                                     double endLng, double endLat, 
                                                     String transportType) {
        // 1. 检查Redis缓存
        String cacheKey = transportInfoCacheService.generateCacheKey(
            startLng, startLat, endLng, endLat, transportType);
        
        Map<String, Object> cachedInfo = transportInfoCacheService.getTransportInfo(cacheKey);
        if (cachedInfo != null) {
            // 缓存命中，直接返回
            TransportInfoVO vo = new TransportInfoVO();
            vo.setDistance((Integer) cachedInfo.get("distance"));
            vo.setDuration((Integer) cachedInfo.get("duration"));
            vo.setTransportType(transportType);
            return Mono.just(vo);
        }

        // 2. 缓存未命中，调用百度地图API
        Mono<JsonNode> routeResult;
        if ("driving".equals(transportType)) {
            routeResult = baiduMapService.getDrivingRoute(startLat, startLng, endLat, endLng);
        } else if ("transit".equals(transportType)) {
            routeResult = baiduMapService.getTransitRoute(startLat, startLng, endLat, endLng);
        } else if ("walking".equals(transportType)) {
            routeResult = baiduMapService.getWalkingRoute(startLat, startLng, endLat, endLng);
        } else {
            return Mono.error(new IllegalArgumentException("不支持的交通方式: " + transportType));
        }

        return routeResult.flatMap(result -> {
            try {
                // 解析百度地图API返回结果
                // result已经是result节点，需要访问routes数组
                JsonNode routes = result.path("routes");
                if (routes == null || !routes.isArray() || routes.size() == 0) {
                    log.warn("百度地图API返回结果中没有路线: transportType={}, result={}", transportType, result);
                    return Mono.error(new RuntimeException("未找到路线规划结果"));
                }

                JsonNode firstRoute = routes.get(0);
                
                // 获取距离和时间（单位：米和秒）
                int distance = firstRoute.path("distance").asInt(0);
                int duration = firstRoute.path("duration").asInt(0);
                
                if (distance == 0 && duration == 0) {
                    log.warn("百度地图API返回的距离或时间为0: transportType={}, route={}", transportType, firstRoute);
                }

                // 创建VO
                TransportInfoVO vo = new TransportInfoVO();
                vo.setDistance(distance);
                vo.setDuration(duration);
                vo.setTransportType(transportType);

                // 3. 保存到Redis缓存
                Map<String, Object> transportData = new HashMap<>();
                transportData.put("distance", distance);
                transportData.put("duration", duration);
                transportInfoCacheService.cacheTransportInfo(cacheKey, transportData);

                return Mono.just(vo);
            } catch (Exception e) {
                log.error("解析百度地图路线规划结果失败: transportType={}, result={}", transportType, result, e);
                return Mono.error(new RuntimeException("解析路线规划结果失败: " + e.getMessage()));
            }
        });
    }
}

