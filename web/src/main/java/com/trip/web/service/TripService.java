package com.trip.web.service;

import com.trip.model.dto.TripCreateDTO;
import com.trip.model.entity.Trip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.TripVO;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface TripService extends IService<Trip> {

    /**
     * 创建行程
     * @param dto 行程信息
     * @param creatorId 创建者ID
     * @return 行程详情
     */
    TripVO createTrip(TripCreateDTO dto, Long creatorId);

    List<PlaceCreateVO> batchImportPlaces(Long tripId, String text);
}
