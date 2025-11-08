package com.trip.web.service;

import com.trip.model.entity.Trip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.PlaceCreateVO;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface TripService extends IService<Trip> {

    List<PlaceCreateVO> batchImportPlaces(Long tripId, String text);
}
