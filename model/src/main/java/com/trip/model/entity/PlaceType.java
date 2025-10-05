package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 地点类型表
 * @TableName place_type
 */
@TableName(value ="place_type")
@Data
public class PlaceType extends BaseEntity {
    /**
     * 类型编码（英文，如 sight/hotel/restaurant/transport）
     */
    private String code;

    /**
     * 类型名称（中文显示）
     */
    private String name;
}