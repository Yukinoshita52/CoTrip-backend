package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.trip.model.dto.DetailInfo;
import lombok.Data;

/**
 * 地点信息表
 * @TableName place
 */
@TableName(value ="place")
@Data
public class Place {
    private Long id;
    private String name;
    private Integer typeId;
    private String uid;
    private Float lat;
    private Float lng;
    private String address;
    private String telephone;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private DetailInfo detailInfo;
}
