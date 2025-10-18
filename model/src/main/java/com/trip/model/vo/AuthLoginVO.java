package com.trip.model.vo;

import lombok.Data;

@Data
public class AuthLoginVO {
    private Long userId;
    private String token;
    private String avatarUrl;
}


