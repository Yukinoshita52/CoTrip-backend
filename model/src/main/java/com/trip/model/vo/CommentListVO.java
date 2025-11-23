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
    private List<CommentVO> comments;
}