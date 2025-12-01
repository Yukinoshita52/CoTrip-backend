package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName book
 */
@TableName(value ="book")
@Data
public class Book extends BaseEntity {
    /**
     * ID
     */
    private Long tripId;

    /**
     * 
     */
    private String name;
}