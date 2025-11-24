package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.TripCreateDTO;
import com.trip.model.vo.TripVO;

import java.util.List;
import com.trip.web.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 行程管理控制器（不需要tripId的操作）
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripsController {

    private final TripService tripService;

    /**
     * 创建行程
     * @param dto 行程信息
     * @return 行程详情
     */
    @PostMapping
    public Result<TripVO> createTrip(@RequestBody @Validated TripCreateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        TripVO tripVO = tripService.createTrip(dto, loginUser.getUserId());
        return Result.ok(tripVO);
    }

    /**
     * 查看行程列表（按用户筛选）
     * @return 行程列表
     */
    @GetMapping
    public Result<List<TripVO>> getTrips() {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        List<TripVO> trips = tripService.getUserTrips(loginUser.getUserId());
        return Result.ok(trips);
    }
}

