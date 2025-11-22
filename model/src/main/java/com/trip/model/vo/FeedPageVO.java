package com.trip.model.vo;

import com.trip.model.entity.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Feed 页返回的数据
 */
@Data
public class FeedPageVO {
    private Integer page;
    private Integer size;
    private Long total;
    private List<FeedItemVO> list;

    @Data
    public static class FeedItemVO {
        private Long postId;
        private Long tripId;
        private String tripName;
        private String region;
        private Date startDate;
        private Date endDate;
        private String description;
        private List<String> coverImages;
        private Author author; // 直接用 User entity
        private Stats stats;
        private Date createTime;

        @Data
        public static class Stats {
            private Integer likeCount;
            private Integer commentCount;
            private Boolean liked; // 可选
        }

        @Data
        public static class Author{
            private Long userId;
            private String nickName;
            private String avatar;
        }
    }
}