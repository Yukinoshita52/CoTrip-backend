package com.trip.model.vo;

import lombok.Data;

/**
 * 帖子收藏结果VO
 */
@Data
public class PostCollectVO {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 收藏数量
     */
    private Long collectCount;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
    
    /**
     * 操作消息
     */
    private String message;
}