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
    private String avatar;
    private Stats stats;
    private List<UserPostVO> posts;

    @Data
    public static class Stats {
        private Integer postCount;
        private Integer totalLikes;
    }

    @Data
    public static class UserPostVO {
        private Long postId;
        private String tripName;
        private List<String> coverImages;
        private Date createTime;
    }
}