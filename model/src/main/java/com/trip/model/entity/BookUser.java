package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * -
 * @TableName book_user
 */
@TableName(value ="book_user")
@Data
public class BookUser extends BaseEntity {
    /**
     * ID
     */
    private Long bookId;

    /**
     * ID
     */
    private Long userId;
}