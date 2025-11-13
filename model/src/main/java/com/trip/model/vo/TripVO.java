package com.trip.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 行程 VO
 */
@Data
public class TripVO {

    private Long tripId;

    private String name;

    private Date startDate;

    private Date endDate;

    private String description;

    private String region;

    private Date createdTime;
}

