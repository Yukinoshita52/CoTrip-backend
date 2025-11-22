package com.trip.model.vo;

import lombok.Data;

/**
 * 点赞返回
 */
@Data
public class PostLikeVO {
    private Long postId;
    private Boolean liked;
    private Integer likeCount;
}