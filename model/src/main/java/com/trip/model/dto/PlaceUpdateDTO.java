package com.trip.model.dto;

import lombok.Data;

@Data
public class PlaceUpdateDTO {
    private String name;
    private String typeCode;
    private Integer day;
    private String notes;
}


