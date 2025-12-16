package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.TripUser;
import com.trip.web.mapper.TripUserMapper;
import com.trip.web.service.AccountService;
import com.trip.web.service.TripUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        // 检查是否已存在
        if (isUserInTrip(tripId, userId)) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "用户已在该行程中");
        }

        TripUser tripUser = new TripUser();
        tripUser.setTripId(tripId);
        tripUser.setUserId(userId);
        tripUser.setRole(1); // 1-参与者
        this.save(tripUser);
        
        // 将新成员添加到该行程的所有账本中
        accountService.addMemberToTripBooks(tripId, userId);
    }

    @Override
    public boolean isUserInTrip(Long tripId, Long userId) {
        long count = this.count(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId));
        return count > 0;
    }
}

