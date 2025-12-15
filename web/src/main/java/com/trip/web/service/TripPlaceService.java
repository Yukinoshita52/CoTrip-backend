package com.trip.web.service;

import com.trip.model.entity.TripPlace;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip_place(行程-地点关系表)】的数据库操作Service
* @createDate 2025-10-05 23:38:17
*/
public interface TripPlaceService extends IService<TripPlace> {

    void deletePlace(Long tripId, Long placeId);

    /**
     * 更新地点顺序
     * @param tripId 行程ID
     * @param placeIds 地点ID列表（按顺序排列）
     */
    void updatePlaceOrder(Long tripId, List<Long> placeIds);

    /**
     * 更新地点信息
     * @param tripId 行程ID
     * @param placeId 地点ID
     * @param day 天数
     * @param typeId 地点类型ID
     */
    void updatePlace(Long tripId, Long placeId, Integer day, Integer typeId);
}
