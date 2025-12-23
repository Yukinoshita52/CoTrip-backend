package com.trip.web.service;

import com.trip.model.vo.AdminStatisticsVO;

/**
 * 管理员统计服务接口
 */
public interface AdminStatisticsService {
    /**
     * 获取系统统计数据
     * @return 统计数据
     */
    AdminStatisticsVO getStatistics();
}

