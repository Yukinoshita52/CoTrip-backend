package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行程表
 * @TableName trip
 */
@TableName(value ="trip")
@Data
@EqualsAndHashCode(callSuper = false)
public class Trip extends BaseEntity {
    /**
     * 行程名称
     */
    private String name;

    /**
     * 开始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 行程描述
     */
    private String description;

    /**
     * 城市
     */
    private String region;
}