package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.TripUser;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.service.AccountService;
import com.trip.web.service.TripCacheService;
import com.trip.web.service.TripUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 26423
 * @description 针对表【trip_user(行程-用户关联表)】的数据库操作Service实现
 * @createDate 2025-11-13
 */
@Service
@RequiredArgsConstructor
public class TripUserServiceImpl extends ServiceImpl<TripUserMapper, TripUser>
        implements TripUserService {
        
    private final AccountService accountService;
    private final TripCacheService tripCacheService;

    @Override
    public void addCreator(Long tripId, Long userId) {
        // 检查是否已存在
        if (isUserInTrip(tripId, userId)) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "用户已在该行程中");
        }

        TripUser tripUser = new TripUser();
        tripUser.setTripId(tripId);
        tripUser.setUserId(userId);
        tripUser.setRole(0); // 0-创建者
        this.save(tripUser);
    }

    @Override
    public void addParticipant(Long tripId, Long userId) {
        System.out.println("addParticipant: 将用户 " + userId + " 添加到行程 " + tripId);
        
        // 检查是否存在已删除的记录（用户之前退出过）
        TripUser existingTripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 1)); // 查找已删除的记录
        
        if (existingTripUser != null) {
            // 恢复已删除的记录
            existingTripUser.setIsDeleted((byte) 0);
            existingTripUser.setRole(2); // 重新设置为参与者（2）
            this.updateById(existingTripUser);
            System.out.println("addParticipant: 恢复用户 " + userId + " 在行程 " + tripId + " 中的记录");
        } else {
            // 检查是否已存在有效记录
            if (isUserInTrip(tripId, userId)) {
                System.out.println("addParticipant: 用户 " + userId + " 已在行程 " + tripId + " 中");
                throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "用户已在该行程中");
            }

            // 创建新记录
            TripUser tripUser = new TripUser();
            tripUser.setTripId(tripId);
            tripUser.setUserId(userId);
            tripUser.setRole(2); // 2-参与者（只读）
            this.save(tripUser);
            System.out.println("addParticipant: 已将用户 " + userId + " 添加到行程 " + tripId);
        }
        
        // 将新成员添加到该行程的所有账本中
        System.out.println("addParticipant: 开始为用户 " + userId + " 添加账本权限");
        accountService.addMemberToTripBooks(tripId, userId);
        System.out.println("addParticipant: 完成为用户 " + userId + " 添加账本权限");

        // 清除相关用户的行程缓存
        List<TripUser> allTripUsers = this.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getIsDeleted, 0));
        List<Long> userIds = allTripUsers.stream()
                .map(TripUser::getUserId)
                .collect(Collectors.toList());
        tripCacheService.evictTripRelatedCache(tripId, userIds);
    }

    @Override
    public void removeParticipant(Long tripId, Long userId) {
        System.out.println("removeParticipant: 用户 " + userId + " 退出行程 " + tripId);
        
        // 检查用户是否在行程中
        TripUser tripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0));
        
        if (tripUser == null) {
            System.out.println("removeParticipant: 用户 " + userId + " 不在行程 " + tripId + " 中");
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "用户不在该行程中");
        }
        
        // 检查是否为创建者
        if (tripUser.getRole() == 0) {
            System.out.println("removeParticipant: 用户 " + userId + " 是行程 " + tripId + " 的创建者，不能退出");
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程创建者不能退出行程");
        }
        
        // 使用MyBatis-Plus的逻辑删除功能
        boolean removed = this.removeById(tripUser.getId());
        if (removed) {
            System.out.println("removeParticipant: 已将用户 " + userId + " 从行程 " + tripId + " 中移除");
        } else {
            System.out.println("removeParticipant: 移除用户 " + userId + " 从行程 " + tripId + " 失败");
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "退出行程失败");
        }
        
        // 从该行程的所有账本中移除用户
        accountService.removeMemberFromTripBooks(tripId, userId);
        System.out.println("removeParticipant: 已将用户 " + userId + " 从行程 " + tripId + " 的账本中移除");

        // 清除相关用户的行程缓存
        List<TripUser> allTripUsers = this.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getIsDeleted, 0));
        List<Long> userIds = allTripUsers.stream()
                .map(TripUser::getUserId)
                .collect(Collectors.toList());
        // 也要清除退出用户的缓存
        userIds.add(userId);
        tripCacheService.evictTripRelatedCache(tripId, userIds);
    }

    @Override
    public void addAdmin(Long tripId, Long userId) {
        System.out.println("addAdmin: 将用户 " + userId + " 添加为行程 " + tripId + " 的管理员");
        
        // 检查是否存在已删除的记录（用户之前退出过）
        TripUser existingTripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 1)); // 查找已删除的记录
        
        if (existingTripUser != null) {
            // 恢复已删除的记录
            existingTripUser.setIsDeleted((byte) 0);
            existingTripUser.setRole(1); // 设置为管理员
            this.updateById(existingTripUser);
            System.out.println("addAdmin: 恢复用户 " + userId + " 在行程 " + tripId + " 中的记录，设置为管理员");
        } else {
            // 检查是否已存在有效记录
            if (isUserInTrip(tripId, userId)) {
                System.out.println("addAdmin: 用户 " + userId + " 已在行程 " + tripId + " 中");
                throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "用户已在该行程中");
            }

            // 创建新记录
            TripUser tripUser = new TripUser();
            tripUser.setTripId(tripId);
            tripUser.setUserId(userId);
            tripUser.setRole(1); // 1-管理员（可写）
            this.save(tripUser);
            System.out.println("addAdmin: 已将用户 " + userId + " 添加为行程 " + tripId + " 的管理员");
        }
        
        // 将新成员添加到该行程的所有账本中
        System.out.println("addAdmin: 开始为用户 " + userId + " 添加账本权限");
        accountService.addMemberToTripBooks(tripId, userId);
        System.out.println("addAdmin: 完成为用户 " + userId + " 添加账本权限");
    }

    @Override
    public boolean isUserInTrip(Long tripId, Long userId) {
        long count = this.count(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0)); // 添加这个条件确保用户未退出行程
        return count > 0;
    }

    @Override
    public boolean hasEditPermission(Long tripId, Long userId) {
        TripUser tripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0));
        
        if (tripUser == null) {
            return false;
        }
        
        // 创建者（0）和管理员（1）有编辑权限
        Integer role = tripUser.getRole();
        return role != null && (role == 0 || role == 1);
    }

    @Override
    public boolean hasMemberManagePermission(Long tripId, Long userId) {
        TripUser tripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0));
        
        if (tripUser == null) {
            return false;
        }
        
        // 创建者（0）和管理员（1）可以管理成员
        Integer role = tripUser.getRole();
        return role != null && (role == 0 || role == 1);
    }

    @Override
    public Integer getUserRole(Long tripId, Long userId) {
        TripUser tripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0));
        
        return tripUser != null ? tripUser.getRole() : null;
    }

    @Override
    public void updateMemberRole(Long tripId, Long targetUserId, Integer newRole, Long operatorUserId) {
        // 检查操作者权限
        if (!hasMemberManagePermission(tripId, operatorUserId)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN.getCode(), "无权限修改成员角色");
        }
        
        // 验证角色值
        if (newRole == null || (newRole != 0 && newRole != 1 && newRole != 2)) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR.getCode(), "角色值无效");
        }
        
        // 不能修改创建者的角色
        TripUser targetTripUser = this.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, targetUserId)
                .eq(TripUser::getIsDeleted, 0));
        
        if (targetTripUser == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "目标用户不在该行程中");
        }
        
        if (targetTripUser.getRole() == 0) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "不能修改创建者的角色");
        }
        
        // 不能将其他用户设置为创建者（创建者角色只能通过创建行程获得）
        if (newRole == 0) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "不能将用户设置为创建者");
        }
        
        // 更新角色
        targetTripUser.setRole(newRole);
        this.updateById(targetTripUser);
        System.out.println("updateMemberRole: 已将用户 " + targetUserId + " 在行程 " + tripId + " 中的角色修改为 " + newRole);
    }
}

