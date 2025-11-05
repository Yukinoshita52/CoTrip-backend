package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.trip.model.entity.Place;
import com.trip.web.service.PlaceService;
import com.trip.web.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.web.service.BaiduMapService;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
public class PlaceServiceImpl extends ServiceImpl<PlaceMapper, Place>
    implements PlaceService{

    private final BaiduMapService baiduMapService;
    private final String region = "全国";

    @Override
    public Mono<Result<JsonNode>> getSuggestions(String query) {
        return baiduMapService.getSuggestions(query, region)
                .map(Result::ok)
                .onErrorResume(e -> Mono.just(Result.fail(ResultCodeEnum.FAIL.getCode(), e.getMessage())));
    }

}
