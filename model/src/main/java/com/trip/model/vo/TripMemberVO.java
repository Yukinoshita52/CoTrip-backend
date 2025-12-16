package com.trip.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 行程成员 VO
 */
@Data
public class TripMemberVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色：0-创建者，1-参与者
     */
    private Integer role;

    /**
     * 加入时间
     */
    private Date joinedAt;
}


