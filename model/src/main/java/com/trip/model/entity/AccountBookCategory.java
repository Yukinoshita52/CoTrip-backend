package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName account_book_category
 */
@TableName(value ="account_book_category")
@Data
public class AccountBookCategory extends BaseEntity{
    /**
     * ID
     */
    private Long userId;

    /**
     * 
     */
    private String name;

    /**
     * 1- 2-
     */
    private Integer type;

    /**
     * 
     */
    private String icon;

    /**
     * 
     */
    private Integer sort;

}