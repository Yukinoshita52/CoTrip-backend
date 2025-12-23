package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.TripPlace;
import com.trip.web.service.TripPlaceService;
import com.trip.web.service.PlaceService;
import com.trip.web.mapper.TripPlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip_place(行程-地点关系表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:17
*/
@Service
@RequiredArgsConstructor
public class TripPlaceServiceImpl extends ServiceImpl<TripPlaceMapper, TripPlace>
    implements TripPlaceService{

    private final PlaceService placeService;
    private final com.trip.web.service.TripUserService tripUserService;

    @Override
    public void deletePlace(Long tripId, Long placeId, Long userId){
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
            throw new com.trip.common.exception.LeaseException(com.trip.common.result.ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权删除地点");
        }
        
        UpdateWrapper<TripPlace> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trip_id", tripId)
                .eq("place_id", placeId);

        TripPlace tripPlace = new TripPlace();
        tripPlace.setIsDeleted((byte) 1);
        this.update(tripPlace, updateWrapper);
    }

    @Override
    public void updatePlaceOrder(Long tripId, List<Long> placeIds, Long userId) {
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
            throw new com.trip.common.exception.LeaseException(com.trip.common.result.ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权修改地点顺序");
        }
        
        // 遍历地点ID列表，按顺序更新sequence字段
        for (int i = 0; i < placeIds.size(); i++) {
            Long placeId = placeIds.get(i);
            TripPlace tripPlace = this.getOne(new LambdaQueryWrapper<TripPlace>()
                    .eq(TripPlace::getTripId, tripId)
                    .eq(TripPlace::getPlaceId, placeId));
            
            if (tripPlace != null) {
                tripPlace.setSequence(i + 1); // sequence从1开始
                this.updateById(tripPlace);
            }
        }
    }

    @Override
    public void updatePlace(Long tripId, Long placeId, Integer day, Integer typeId, Long userId) {
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
            throw new com.trip.common.exception.LeaseException(com.trip.common.result.ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权修改地点");
        }
        
        // 更新TripPlace表中的day字段
        TripPlace tripPlace = this.getOne(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId)
                .eq(TripPlace::getPlaceId, placeId));
        
        if (tripPlace != null) {
            tripPlace.setDay(day);
            this.updateById(tripPlace);
        }

        // 如果提供了typeId，更新Place表中的typeId字段
        if (typeId != null) {
            placeService.updatePlaceType(placeId, typeId);
        }
    }

}




