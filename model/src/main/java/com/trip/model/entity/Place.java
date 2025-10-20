package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 地点信息表
 * @TableName place
 */
@TableName(value ="place")
@Data
public class Place extends BaseEntity {
    /**
     * 关联行程ID
     */
    private Long tripId;

    /**
     * 地点名称
     */
    private String name;

    /**
     * 地点类型ID，关联 place_type(id)
     */
    private Integer typeId;

    /**
     * 第几天
     */
    private Integer day;

    /**
     * 备注
     */
    private String notes;
}