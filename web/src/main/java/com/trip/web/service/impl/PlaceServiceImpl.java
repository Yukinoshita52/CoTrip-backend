package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Place;
import com.trip.web.service.PlaceService;
import com.trip.web.mapper.PlaceMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class PlaceServiceImpl extends ServiceImpl<PlaceMapper, Place>
    implements PlaceService{

}




