package com.trip.model.vo;

import lombok.Data;

/**
 * 交通信息VO
 */
@Data
public class TransportInfoVO {
    /**
     * 距离（米）
     */
    private Integer distance;

    /**
     * 时间（秒）
     */
    private Integer duration;

    /**
     * 交通方式
     */
    private String transportType;
}

