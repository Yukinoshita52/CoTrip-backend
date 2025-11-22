package com.trip.model.vo;

import lombok.Data;
import java.util.Date;
/**
 * 新增评论返回
 */
@Data
public class CommentCreatedVO {
    private Long commentId;
    private Long postId;
    private Date createTime;
}