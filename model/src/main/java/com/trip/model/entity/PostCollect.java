package com.trip.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子收藏表
 * @TableName post_collect
 */
@TableName(value = "post_collect")
@Data
@EqualsAndHashCode(callSuper = false)
public class PostCollect extends BaseEntity {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 用户ID
     */
    private Long userId;
}