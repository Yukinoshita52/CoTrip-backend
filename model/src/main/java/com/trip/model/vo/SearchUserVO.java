package com.trip.model.vo;

import com.trip.model.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 搜索用户
 */
@Data
public class SearchUserVO {
    private String keyword;
    private List<AuthorVO> users;
}