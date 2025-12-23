package com.trip.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户主页
 */
@Data
public class UserProfileVO {
    private Long userId;
    private String nickname;
    private String username;
    private String avatar;
    private UserPostsStatsVO stats;
    private List<UserPostVO> posts;
}