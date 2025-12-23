package com.trip.model.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String phone;
    /**
     * 用户角色：0-普通用户，1-管理员
     */
    private Integer role;
}


