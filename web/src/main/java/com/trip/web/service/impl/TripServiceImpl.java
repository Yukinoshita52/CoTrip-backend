package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Trip;
import com.trip.web.mapper.TripMapper;
import com.trip.web.service.TripService;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip>
        implements TripService{

}




