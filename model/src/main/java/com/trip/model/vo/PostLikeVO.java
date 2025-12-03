package com.trip.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * 点赞返回
 */
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeVO {
    private Long postId;
    private Boolean liked;
    private Integer likeCount;
}