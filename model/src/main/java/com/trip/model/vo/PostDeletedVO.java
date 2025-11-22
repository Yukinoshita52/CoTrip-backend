package com.trip.model.vo;

import lombok.Data;

/**
 * 删除帖子返回
 */
@Data
public class PostDeletedVO {
    private Long postId;
    private Boolean deleted;
}