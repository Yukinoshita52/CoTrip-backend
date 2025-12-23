package com.trip.model.vo;

import lombok.Data;

/**
 * 管理员统计数据VO
 */
@Data
public class AdminStatisticsVO {
    /**
     * 用户总数
     */
    private Long totalUsers;
    
    /**
     * 行程总数
     */
    private Long totalTrips;
    
    /**
     * 社区帖子总数
     */
    private Long totalPosts;
}

