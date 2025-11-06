package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.trip.model.entity.Place;
import com.trip.web.mapper.TripMapper;
import com.trip.web.service.PlaceService;
import com.trip.web.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.web.service.BaiduMapService;
import com.trip.model.dto.SuggestionDTO;
import java.util.ArrayList;
import java.util.List;

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
    private final TripMapper tripMapper;

    @Override
    public Result<List<SuggestionDTO>> getSuggestions(String query, Long tripId) {
        String region = tripMapper.selectById(tripId).getRegion();

        try {
            // 调用百度接口
            JsonNode resultArray = baiduMapService.getSuggestions(query, region)
                    .block();

            List<SuggestionDTO> suggestions = new ArrayList<>();
            if (resultArray != null && resultArray.isArray()) {
                for (JsonNode item : resultArray) {
                    String uid = item.path("uid").asText(null);
                    String name = item.path("name").asText(null);
                    if (uid != null && name != null) {
                        suggestions.add(new SuggestionDTO(uid, name));
                    }
                }
            }

            return Result.ok(suggestions);
        } catch (Exception e) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), e.getMessage());
        }
    }

}
