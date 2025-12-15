package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.exception.LeaseException;
import com.trip.model.dto.DetailInfo;
import com.trip.model.dto.PlaceCreateDTO;
import com.trip.model.entity.Place;
import com.trip.model.entity.TripPlace;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.PlaceDetailVO;
import com.trip.web.mapper.TripMapper;
import com.trip.web.mapper.TripPlaceMapper;
import com.trip.web.service.PlaceService;
import com.trip.web.mapper.PlaceMapper;
import com.trip.web.service.PlaceTypeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
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
    private final PlaceTypeService placeTypeService;
    private final TripMapper tripMapper;
    private final TripPlaceMapper tripPlaceMapper;

    @Override
    @Cacheable(
            value = "suggestions",
            key = "#tripId + ':' + #query",      // 缓存 key
            unless = "#result == null || #result.size() == 0"
    )
    public List<SuggestionDTO> getSuggestions(String query, Long tripId) {
        String region = tripMapper.selectById(tripId).getRegion();

        try {
            // 调用百度接口
            JsonNode resultArray = baiduMapService.getSuggestions(query, region).block();

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

            return suggestions;
        } catch (Exception e) {
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "地点搜索失败");
        }
    }

    @Override
    public PlaceCreateVO addPlace(Long tripId, PlaceCreateDTO placeCreateDTO) {
        JsonNode result = baiduMapService.getPlaceDetail(placeCreateDTO.getUid()).block();
        if (result == null || result.isMissingNode()) {
            log.warn("未获取到地点详情");
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "添加地点失败");
        }

        // 提取字段
        String uid = result.path("uid").asText();
        String name = result.path("name").asText();
        // 使用用户选择的类型ID，如果没有选择则自动判断
        Integer typeId = placeCreateDTO.getTypeId() != null ? 
            placeCreateDTO.getTypeId() : 
            placeTypeService.determineTypeId(name);
        Float lat = (float) result.path("location").path("lat").asDouble();
        Float lng = (float) result.path("location").path("lng").asDouble();
        String address = result.path("address").asText("");
        String telephone = result.path("telephone").asText("");
        
        DetailInfo detailInfo;
        try {
            detailInfo = new ObjectMapper().treeToValue(result.path("detail_info"), DetailInfo.class);
        } catch (JsonProcessingException e) {
            log.warn("地点详情反序列化失败: " + e.getMessage());
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "添加地点失败");
        }

        // 检查是否已存在
        Place place = this.lambdaQuery().eq(Place::getUid, uid).one();
        if (place == null) {
            place = new Place();
            place.setUid(uid);
            place.setName(name);
            place.setTypeId(typeId);
            place.setLat(lat);
            place.setLng(lng);
            place.setAddress(address);
            place.setTelephone(telephone);
            place.setDetailInfo(detailInfo);
            this.save(place); // 插入数据库
        }
        Long placeId = place.getId();

        // 更新 trip_place
        TripPlace existingTp = tripPlaceMapper.selectOne(
                new QueryWrapper<TripPlace>()
                        .eq("trip_id", tripId)
                        .eq("place_id", placeId)
        );

        // trip 中已经存在该 place
        if (existingTp != null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程已包含此地点");
        }

        // 插入新记录
        TripPlace tp = new TripPlace();
        tp.setTripId(tripId);
        tp.setPlaceId(placeId);
        tp.setDay(placeCreateDTO.getDay());
        tripPlaceMapper.insert(tp);

        PlaceCreateVO vo = new PlaceCreateVO();
        vo.setId(place.getId());
        vo.setName(place.getName());
        vo.setType(placeTypeService.getTypeNameById(place.getTypeId()));
        vo.setLat(place.getLat());
        vo.setLng(place.getLng());

        return vo;
    }

    public PlaceDetailVO getPlaceDetails(Long placeId){
        Place place = this.getById(placeId);
        if(place == null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "地点不存在");
        }

        PlaceDetailVO vo = new PlaceDetailVO();
        vo.setId(place.getId());
        vo.setName(place.getName());
        vo.setType(placeTypeService.getTypeNameById(place.getTypeId()));
        vo.setLat(place.getLat());
        vo.setLng(place.getLng());
        vo.setAddress(place.getAddress());
        vo.setTelephone(place.getTelephone());
        vo.setDetailInfo(place.getDetailInfo());

        return vo;
    }

    @Override
    public void updatePlaceType(Long placeId, Integer typeId) {
        Place place = this.getById(placeId);
        if (place != null && typeId != null) {
            place.setTypeId(typeId);
            this.updateById(place);
        }
    }
}
