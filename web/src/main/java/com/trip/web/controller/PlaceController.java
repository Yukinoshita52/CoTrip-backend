package com.trip.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.common.result.Result;
import com.trip.model.entity.Place;
import com.trip.web.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@Tag(name = "Place Management", description = "Endpoints for managing places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    @Operation(summary = "List places with pagination")
    public Result<Page<Place>> listPlaces(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Place> placePage = placeService.page(new Page<>(page, size));
        return Result.ok(placePage);
    }

    @PostMapping
    @Operation(summary = "Create a new place")
    public Result<Void> createPlace(@RequestBody @Validated Place place) {
        placeService.save(place);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing place")
    public Result<Void> updatePlace(@PathVariable Long id, @RequestBody @Validated Place place) {
        place.setId(id);
        placeService.updateById(place);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a place")
    public Result<Void> deletePlace(@PathVariable Long id) {
        placeService.removeById(id);
        return Result.ok();
    }
}
