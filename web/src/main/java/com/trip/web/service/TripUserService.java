package com.trip.web.service;

import com.trip.model.entity.TripUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 26423
 * @description 针对表【trip_user(行程-用户关联表)】的数据库操作Service
 * @createDate 2025-11-13
 */
public interface TripUserService extends IService<TripUser> {

    /**
     * 添加行程创建者
     * @param tripId 行程ID
     * @param userId 用户ID
     */
    void addCreator(Long tripId, Long userId);

    /**
     * 添加行程参与者
     * @param tripId 行程ID
     * @param userId 用户ID
     */
    void addParticipant(Long tripId, Long userId);

    /**
     * 移除行程参与者（退出行程）
     * @param tripId 行程ID
     * @param userId 用户ID
     */
    void removeParticipant(Long tripId, Long userId);

    /**
     * 检查用户是否已在行程中
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return 是否已存在
     */
    boolean isUserInTrip(Long tripId, Long userId);
}

