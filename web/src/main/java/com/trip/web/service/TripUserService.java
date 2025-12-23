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
     * 添加行程管理员
     * @param tripId 行程ID
     * @param userId 用户ID
     */
    void addAdmin(Long tripId, Long userId);

    /**
     * 检查用户是否已在行程中
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return 是否已存在
     */
    boolean isUserInTrip(Long tripId, Long userId);

    /**
     * 检查用户是否有编辑权限（创建者或管理员）
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return 是否有编辑权限
     */
    boolean hasEditPermission(Long tripId, Long userId);

    /**
     * 检查用户是否有权限管理成员（创建者或管理员）
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return 是否有权限管理成员
     */
    boolean hasMemberManagePermission(Long tripId, Long userId);

    /**
     * 获取用户在行程中的角色
     * @param tripId 行程ID
     * @param userId 用户ID
     * @return 角色：0-创建者，1-管理员，2-参与者，null-不在行程中
     */
    Integer getUserRole(Long tripId, Long userId);

    /**
     * 修改成员角色（创建者和管理员可以修改）
     * @param tripId 行程ID
     * @param targetUserId 目标用户ID
     * @param newRole 新角色：0-创建者，1-管理员，2-参与者
     * @param operatorUserId 操作者用户ID
     */
    void updateMemberRole(Long tripId, Long targetUserId, Integer newRole, Long operatorUserId);
}

