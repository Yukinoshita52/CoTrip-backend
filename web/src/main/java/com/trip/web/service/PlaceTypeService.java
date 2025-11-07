package com.trip.web.service;

import com.trip.model.entity.PlaceType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 26423
* @description 针对表【place_type(地点类型表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface PlaceTypeService extends IService<PlaceType> {
    Integer determineTypeId(String placeName);
    String getTypeNameById(Integer typeId);
}
