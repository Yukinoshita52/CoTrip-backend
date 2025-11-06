package com.trip.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.Place;
import com.trip.model.dto.SuggestionDTO;
import com.trip.web.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

//    @GetMapping
//    public Result<Page<Place>> listPlaces(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<Place> placePage = placeService.page(new Page<>(page, size));
//        return Result.ok(placePage);
//    }

    // 地点输入提示
    @GetMapping("/suggestion")
    public Result<List<SuggestionDTO>> getSuggestions(
            @RequestParam String query,
            @PathVariable Long tripId) {

        if (query == null || query.trim().isEmpty()) {
            return Result.fail(ResultCodeEnum.FAIL.getCode(), "请输入查询关键字");
        }

        return placeService.getSuggestions(query.trim(), tripId);
    }

//    @PostMapping("/{id}")
//    public Result<Void> addPlace(@PathVariable Long id, @RequestBody @Validated Place place) {
//        place.setId(id);
//        placeService.updateById(place);
//        return Result.ok();
//    }
//
//    @DeleteMapping("/{id}")
//    public Result<Void> deletePlace(@PathVariable Long id) {
//        placeService.removeById(id);
//        return Result.ok();
//    }
}
