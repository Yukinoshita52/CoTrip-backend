package com.trip.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.common.result.Result;
import com.trip.model.entity.PlaceType;
import com.trip.web.service.PlaceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place-types")
@RequiredArgsConstructor
public class PlaceTypeController {

    private final PlaceTypeService placeTypeService;

    /**
     * 获取所有地点类型
     * @return 地点类型列表
     */
    @GetMapping
    public Result<List<PlaceType>> getAllPlaceTypes() {
        List<PlaceType> placeTypes = placeTypeService.list();
        return Result.ok(placeTypes);
    }

    /**
     * 分页获取地点类型
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<PlaceType>> listPlaceTypes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PlaceType> placeTypePage = placeTypeService.page(new Page<>(page, size));
        return Result.ok(placeTypePage);
    }
}
