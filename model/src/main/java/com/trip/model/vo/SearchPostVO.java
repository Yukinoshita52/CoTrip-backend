package com.trip.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 搜索帖子
 */
@Data
public class SearchPostVO {
    private String keyword;
    private List<SearchItemVO> results;

    @Data
    public static class SearchItemVO {
        private Long postId;
        private String tripName;
        private String region;
        private List<String> coverImages;
    }
}