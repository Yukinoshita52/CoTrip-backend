package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName account_book_record
 */
@TableName(value ="account_book_record")
@Data
public class AccountBookRecord extends BaseEntity{
    /**
     * ID
     */
    private Long bookId;


    private Long userId;

    /**
     * ID
     */
    private Long categoryId;

    /**
     * 
     */
    private BigDecimal amount;

    /**
     * 1- 2-
     */
    private Integer type;

    /**
     * 
     */
    private Date recordTime;

    /**
     * 
     */
    private String remark;
}