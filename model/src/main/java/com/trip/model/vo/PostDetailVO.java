package com.trip.model.vo;

import com.trip.model.entity.Trip;
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
    private AuthorVO author;
    private StatVO stats;
    private Date createTime;

    @Data
    public static class TripDetailVO {
        private Long tripId;
        private String name;
        private String region;
        private Date startDate;
        private Date endDate;
        private String description;
        private List<PlaceDayTypeVO> days;
        private List<String> images;
    }
}