package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.TripPlace;
import com.trip.web.service.TripPlaceService;
import com.trip.web.mapper.TripPlaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip_place(行程-地点关系表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:17
*/
@Service
public class TripPlaceServiceImpl extends ServiceImpl<TripPlaceMapper, TripPlace>
    implements TripPlaceService{

    @Override
    public void deletePlace(Long tripId, Long placeId){
        UpdateWrapper<TripPlace> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trip_id", tripId)
                .eq("place_id", placeId);

        TripPlace tripPlace = new TripPlace();
        tripPlace.setIsDeleted((byte) 1);
        this.update(tripPlace, updateWrapper);
    }

    @Override
    public void updatePlaceOrder(Long tripId, List<Long> placeIds) {
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

}




