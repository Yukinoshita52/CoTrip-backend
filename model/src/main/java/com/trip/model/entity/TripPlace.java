package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 行程-地点关系表
 * @TableName trip_place
 */
@TableName(value ="trip_place")
@Data
public class TripPlace extends BaseEntity{
    /**
     * 行程ID
     */
    private Long tripId;

    /**
     * 地点ID
     */
    private Long placeId;
}