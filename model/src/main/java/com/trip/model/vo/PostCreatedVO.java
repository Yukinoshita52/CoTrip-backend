package com.trip.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 发布帖子返回
 */
@Data
public class PostCreatedVO {
    private Long postId;
    private Long tripId;
    private Date createTime;
}