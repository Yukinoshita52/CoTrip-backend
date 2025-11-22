package com.trip.web.mapper;

import com.trip.model.entity.Place;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.PlaceDayTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 26423
* @description 针对表【place(地点信息表)】的数据库操作Mapper
* @createDate 2025-10-05 23:38:16
* @Entity com.trip.model.entity.Place
*/
public interface PlaceMapper extends BaseMapper<Place> {

    /**
     * 根据tripId从trip_place关系表、place表、place_type表中查询数据
     * @param tripId
     * @return 返回类型为PlaceDayTypeVO所需的数据
     */
    List<PlaceDayTypeVO> getPlaceDayTypeByTripId(@Param("tripId") Long tripId);
}




