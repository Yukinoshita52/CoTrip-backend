package com.trip.web.service;

import com.trip.model.entity.Place;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.JsonNode;
import com.trip.common.result.Result;
import reactor.core.publisher.Mono;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface PlaceService extends IService<Place> {

    // 地点输入提示，交由服务层调用百度异步API并封装为Result
    Mono<Result<JsonNode>> getSuggestions(String query);

}
