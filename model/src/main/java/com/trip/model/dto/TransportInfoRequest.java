package com.trip.model.dto;

import lombok.Data;

/**
 * 交通信息查询请求DTO
 */
@Data
public class TransportInfoRequest {
    /**
     * 起点经度
     */
    private Double startLng;

    /**
     * 起点纬度
     */
    private Double startLat;

    /**
     * 终点经度
     */
    private Double endLng;

    /**
     * 终点纬度
     */
    private Double endLat;

    /**
     * 交通方式：driving(驾车)、transit(公交)、walking(步行)
     */
    private String transportType;
}

