package com.trip.model.vo;

import lombok.Data;

/**
 * 删除评论返回
 */
@Data
public class CommentDeletedVO {
    private Long commentId;
    private Boolean deleted;
}