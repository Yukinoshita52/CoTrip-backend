package com.trip.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.common.result.Result;
import com.trip.model.entity.Place;
import com.trip.model.dto.SuggestionDTO;
import reactor.core.publisher.Mono;

import java.util.List;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface PlaceService extends IService<Place> {

    Result<List<SuggestionDTO>> getSuggestions(String query, Long tripId);
}