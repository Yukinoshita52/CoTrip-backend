package com.trip.model.vo;

import com.trip.model.entity.Comment;
import lombok.Data;

import java.util.List;

/**
 * 评论列表
 */
@Data
public class CommentListVO {
    private Long postId;
    private List<Comment> comments; // 用 entity，子评论可在 Comment entity 中包含 children
}