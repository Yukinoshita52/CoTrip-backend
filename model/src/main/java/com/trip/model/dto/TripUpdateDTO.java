package com.trip.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * 修改行程信息 DTO
 */
@Data
public class TripUpdateDTO {

    private String name;

    private Date startDate;

    private Date endDate;

    private String description;

    private String region;
}
