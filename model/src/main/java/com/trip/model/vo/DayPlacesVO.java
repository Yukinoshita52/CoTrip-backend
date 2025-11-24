package com.trip.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 某一天的地点列表 VO
 */
@Data
public class DayPlacesVO {
    
    /**
     * 第几天（0表示待规划）
     */
    private Integer day;
    
    /**
     * 该天的地点列表
     */
    private List<PlaceInTripVO> places;
}

