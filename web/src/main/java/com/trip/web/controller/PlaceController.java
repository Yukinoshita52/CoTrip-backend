package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.PlaceCreateDTO;
import com.trip.model.dto.SuggestionDTO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.PlaceDetailVO;
import com.trip.web.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 地点输入提示
    @GetMapping("/suggestion")
    public Result<List<SuggestionDTO>> getSuggestions(
            @PathVariable Long tripId,
            @RequestParam String query){

        if (query == null || query.trim().isEmpty()) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "请输入查询关键字");
        }

        return Result.ok(placeService.getSuggestions(query.trim(), tripId));
    }

    // 添加地点
    @PostMapping("/add")
    public Result<PlaceCreateVO> addPlace(@PathVariable Long tripId, @RequestBody @Validated PlaceCreateDTO placeCreateDTO) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        return Result.ok(placeService.addPlace(tripId, placeCreateDTO, loginUser.getUserId()));
    }

    // 查看地点详情
    @GetMapping("/{placeId}")
    public Result<PlaceDetailVO> getPlaceDetails(@PathVariable Long placeId, @PathVariable String tripId) {
        return Result.ok(placeService.getPlaceDetails(placeId));
    }
}
