package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.TripPlace;
import com.trip.web.service.TripPlaceService;
import com.trip.web.mapper.TripPlaceMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【trip_place(行程-地点关系表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:17
*/
@Service
public class TripPlaceServiceImpl extends ServiceImpl<TripPlaceMapper, TripPlace>
    implements TripPlaceService{

}




