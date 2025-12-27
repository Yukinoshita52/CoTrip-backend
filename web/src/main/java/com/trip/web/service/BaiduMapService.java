package com.trip.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaiduMapService {

    @Value("${baidu.map.ak}")
    private String baiduAk;

    private final WebClient baiduWebClient;
    private final ObjectMapper objectMapper;

    /**
     * 地点输入提示
     */
    public Mono<JsonNode> getSuggestions(String query, String region) {
        return baiduWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/v2/suggestion")
                        .queryParam("query", query)
                        .queryParam("region", region)
                        .queryParam("city_limit", "true")
                        .queryParam("output", "json")
                        .queryParam("ak", baiduAk)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(json -> {
                    if (json == null || json.path("status").asInt() != 0) {
                        log.warn("百度输入提示API调用失败: query={}, region={}, reason={}", query, region, json != null ? json.path("message").asText() : "返回为空");
                        return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "百度输入提示API调用失败"));
                    }
                    return Mono.just(json.path("result"));
                });
    }

    /**
     * 地点详情检索
     */
    public Mono<JsonNode> getPlaceDetail(String uid) {
        return baiduWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/place/v2/detail")
                        .queryParam("uid", uid)
                        .queryParam("output", "json")
                        .queryParam("scope", "2")
                        .queryParam("ak", baiduAk)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(json -> {
                    if (json == null || json.path("status").asInt() != 0) {
                        log.warn("百度地点详情API调用失败: uid={}, reason={}", uid, json != null ? json.path("message").asText() : "返回为空");
                        return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "百度地点详情API调用失败"));
                    }
                    return Mono.just(json.path("result"));
                });
    }

    /**
     * 路线规划（驾车）
     * 
     * @param startLat 起点纬度
     * @param startLng 起点经度
     * @param endLat 终点纬度
     * @param endLng 终点经度
     * @return 路线规划结果，包含distance和duration
     */
    public Mono<JsonNode> getDrivingRoute(double startLat, double startLng, double endLat, double endLng) {
        String origin = startLat + "," + startLng;
        String destination = endLat + "," + endLng;
        
        return baiduWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/directionlite/v1/driving")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("ak", baiduAk)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        JsonNode json = objectMapper.readTree(responseBody);
                        if (json == null || json.path("status").asInt() != 0) {
                            log.warn("百度驾车路线规划API调用失败: origin={}, destination={}, reason={}", 
                                origin, destination, json != null ? json.path("message").asText() : "返回为空");
                            return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "百度驾车路线规划API调用失败"));
                        }
                        return Mono.just(json.path("result"));
                    } catch (Exception e) {
                        log.error("解析百度驾车路线规划API响应失败: origin={}, destination={}", origin, destination, e);
                        return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "解析百度驾车路线规划API响应失败"));
                    }
                });
    }

    /**
     * 路线规划（公交）
     * 
     * @param startLat 起点纬度
     * @param startLng 起点经度
     * @param endLat 终点纬度
     * @param endLng 终点经度
     * @return 路线规划结果，包含distance和duration
     */
    public Mono<JsonNode> getTransitRoute(double startLat, double startLng, double endLat, double endLng) {
        String origin = startLat + "," + startLng;
        String destination = endLat + "," + endLng;
        
        return baiduWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/directionlite/v1/transit")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("ak", baiduAk)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        JsonNode json = objectMapper.readTree(responseBody);
                        if (json == null || json.path("status").asInt() != 0) {
                            log.warn("百度公交路线规划API调用失败: origin={}, destination={}, reason={}", 
                                origin, destination, json != null ? json.path("message").asText() : "返回为空");
                            return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "百度公交路线规划API调用失败"));
                        }
                        return Mono.just(json.path("result"));
                    } catch (Exception e) {
                        log.error("解析百度公交路线规划API响应失败: origin={}, destination={}", origin, destination, e);
                        return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "解析百度公交路线规划API响应失败"));
                    }
                });
    }

    /**
     * 路线规划（步行）
     * 
     * @param startLat 起点纬度
     * @param startLng 起点经度
     * @param endLat 终点纬度
     * @param endLng 终点经度
     * @return 路线规划结果，包含distance和duration
     */
    public Mono<JsonNode> getWalkingRoute(double startLat, double startLng, double endLat, double endLng) {
        String origin = startLat + "," + startLng;
        String destination = endLat + "," + endLng;
        
        return baiduWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/directionlite/v1/walking")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("ak", baiduAk)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        JsonNode json = objectMapper.readTree(responseBody);
                        if (json == null || json.path("status").asInt() != 0) {
                            log.warn("百度步行路线规划API调用失败: origin={}, destination={}, reason={}", 
                                origin, destination, json != null ? json.path("message").asText() : "返回为空");
                            return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "百度步行路线规划API调用失败"));
                        }
                        return Mono.just(json.path("result"));
                    } catch (Exception e) {
                        log.error("解析百度步行路线规划API响应失败: origin={}, destination={}", origin, destination, e);
                        return Mono.error(new LeaseException(ResultCodeEnum.FAIL.getCode(), "解析百度步行路线规划API响应失败"));
                    }
                });
    }
}
