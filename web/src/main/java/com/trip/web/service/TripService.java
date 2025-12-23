package com.trip.web.service;

import com.trip.model.dto.TripCreateDTO;
import com.trip.model.dto.TripUpdateDTO;
import com.trip.model.entity.Trip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.TripDetailVO;
import com.trip.model.vo.TripVO;

import java.util.List;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface TripService extends IService<Trip> {

    /**
     * 创建行程
     * @param dto 行程信息
     * @param creatorId 创建者ID
     * @return 行程详情
     */
    TripVO createTrip(TripCreateDTO dto, Long creatorId);

    /**
     * 批量导入地点
     * @param tripId 行程ID
     * @param text 文本内容
     * @param userId 用户ID（用于权限验证）
     * @return 地点列表
     */
    List<PlaceCreateVO> batchImportPlaces(Long tripId, String text, Long userId);

    /**
     * 删除行程及关联数据
     * @param tripId 行程ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteTrip(Long tripId, Long userId);

    /**
     * 修改行程信息
     * @param tripId 行程ID
     * @param dto 行程更新信息
     * @param userId 用户ID（用于权限验证）
     * @return 更新后的行程信息
     */
    TripVO updateTrip(Long tripId, TripUpdateDTO dto, Long userId);

    /**
     * 获取用户的行程列表
     * @param userId 用户ID
     * @return 行程列表
     */
    List<TripVO> getUserTrips(Long userId);

    /**
     * 获取行程详情（含地点列表）
     * @param tripId 行程ID
     * @param userId 用户ID（用于权限验证）
     * @return 行程详情
     */
    TripDetailVO getTripDetail(Long tripId, Long userId);

    /**
     * 一键规划行程路线
     * @param tripId 行程ID
     * @param userId 用户ID（用于权限验证）
     * @return 规划结果消息
     */
    String autoPlanRoute(Long tripId, Long userId);
}
