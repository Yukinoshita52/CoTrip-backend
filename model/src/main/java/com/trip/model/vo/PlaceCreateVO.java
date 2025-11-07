package com.trip.model.vo;

import com.trip.model.dto.DetailInfo;
import lombok.Data;

@Data
public class PlaceCreateVO {
    private Long id;
    private String name;
    private String type;
    private String uid;
    private Float lat;
    private Float lng;
    private String address;
    private String telephone;
    private DetailInfo detailInfo;
    private Integer day;
}

