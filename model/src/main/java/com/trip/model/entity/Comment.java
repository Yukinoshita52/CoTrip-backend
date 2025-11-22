package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment extends BaseEntity{
    /**
     * ID share.id
     */
    private Long postId;

    /**
     * ID user.id
     */
    private Long userId;

    /**
     * 
     */
    private String content;

    /**
     * ID
     */
    private Long parentId;
}