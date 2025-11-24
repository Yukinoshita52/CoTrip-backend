package com.trip.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 行程详情 VO（含地点列表）
 */
@Data
public class TripDetailVO {

    private Long tripId;

    private String name;

    private Date startDate;

    private Date endDate;

    private String description;

    private String region;

    private Date createdTime;

    /**
     * 地点列表，按天数分组
     */
    private List<DayPlacesVO> places;
}

