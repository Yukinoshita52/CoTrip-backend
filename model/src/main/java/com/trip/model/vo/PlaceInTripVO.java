package com.trip.model.vo;

import lombok.Data;

/**
 * 行程中的地点 VO
 */
@Data
public class PlaceInTripVO {
    
    private Long id;
    
    private String name;
    
    private String type;
    
    private Float lat;
    
    private Float lng;
    
    private String address;
    
    /**
     * 顺序
     */
    private Integer sequence;
    
    /**
     * 备注
     */
    private String notes;
}

