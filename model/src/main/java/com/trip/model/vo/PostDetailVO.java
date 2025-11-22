package com.trip.model.vo;

import lombok.Data;
import com.trip.model.entity.User;

import java.util.Date;
import java.util.List;

/**
 * 帖子详情
 */
@Data
public class PostDetailVO {
    private Long postId;
    private TripDetailVO trip;
    private User author; // 用 entity
    private FeedPageVO.FeedItemVO.Stats stats;
    private Date createTime;

    @Data
    public static class TripDetailVO {
        private Long tripId;
        private String name;
        private String region;
        private Date startDate;
        private Date endDate;
        private String description;
        private List<TripDayVO> days;
        private List<String> images;

        @Data
        public static class TripDayVO {
            private Integer day;
            private List<PlaceInfo> places;
        }

        @Data
        public static class PlaceInfo {
            private Long placeId;
            private String name;
            private Double lat;
            private Double lng;
            private String address;
            private String type;
        }
    }
}