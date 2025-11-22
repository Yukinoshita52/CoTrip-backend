package com.trip.web.mapper;

import com.trip.model.entity.TripUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.model.vo.AuthorVO;
import org.apache.ibatis.annotations.Param;

/**
 * @author 26423
 * @description 针对表【trip_user(行程-用户关联表)】的数据库操作Mapper
 * @createDate 2025-11-13
 * @Entity com.trip.model.entity.TripUser
 */
public interface TripUserMapper extends BaseMapper<TripUser> {

    AuthorVO getAuthorByTripId(@Param("tripId") Long id);
}

