package com.trip.model.vo;

import com.trip.model.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 查询点赞用户
 */
@Data
public class PostLikeUsersVO {
    private Long postId;
    private List<User> users; // 用 entity
}
