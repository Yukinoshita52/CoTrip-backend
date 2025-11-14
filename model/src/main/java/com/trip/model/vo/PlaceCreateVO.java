package com.trip.model.vo;

import lombok.Data;

@Data
public class PlaceCreateVO {
    private Long id;
    private String name;
    private String type;
    private Float lat;
    private Float lng;
}

