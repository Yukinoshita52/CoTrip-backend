package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 行程表
 * @TableName trip
 */
@TableName(value ="trip")
@Data
public class Trip {
    @TableId
    private Long id;
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
}