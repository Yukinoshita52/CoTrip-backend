package com.trip.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.dto.PlaceCreateDTO;
import com.trip.model.entity.Place;
import com.trip.model.dto.SuggestionDTO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.PlaceDetailVO;

import java.util.List;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface PlaceService extends IService<Place> {

    List<SuggestionDTO> getSuggestions(String query, Long tripId);
    PlaceCreateVO addPlace(Long tripId, PlaceCreateDTO placeCreateDTO);
    PlaceDetailVO getPlaceDetails(Long placeId);
    
    /**
     * 更新地点类型
     * @param placeId 地点ID
     * @param typeId 新的类型ID
     */
    void updatePlaceType(Long placeId, Integer typeId);
}