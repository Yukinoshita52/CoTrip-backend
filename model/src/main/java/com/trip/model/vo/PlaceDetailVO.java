package com.trip.model.vo;

import com.trip.model.dto.DetailInfo;
import lombok.Data;

@Data
public class PlaceDetailVO {
    private Long id;
    private String name;
    private String type;
    private Float lat;
    private Float lng;
    private String address;
    private String telephone;
    private DetailInfo detailInfo;
}
