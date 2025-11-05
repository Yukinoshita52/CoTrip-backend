package com.trip.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.Place;
import com.trip.web.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public Result<Page<Place>> listPlaces(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Place> placePage = placeService.page(new Page<>(page, size));
        return Result.ok(placePage);
    }

    // 地点输入提示
    @GetMapping("/suggestion")
    public Mono<Result<JsonNode>> getSuggestions(@RequestParam String query) {
        // 防止空查询
        if (query == null || query.trim().isEmpty()) {
            return Mono.just(Result.fail(ResultCodeEnum.FAIL.getCode(), "请输入查询关键字"));
        }

        return placeService.getSuggestions(query.trim());
    }

    @PutMapping("/{id}")
    public Result<Void> updatePlace(@PathVariable Long id, @RequestBody @Validated Place place) {
        place.setId(id);
        placeService.updateById(place);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePlace(@PathVariable Long id) {
        placeService.removeById(id);
        return Result.ok();
    }
}
