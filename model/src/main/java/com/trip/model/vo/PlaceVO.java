package com.trip.model.vo;

import lombok.Data;

@Data
public class PlaceVO {
    private Long id;
    private Long tripId;
    private String name;
    private String typeCode;
    private Integer day;
    private String notes;
    private String createdTime;
    private String updatedTime;
}


