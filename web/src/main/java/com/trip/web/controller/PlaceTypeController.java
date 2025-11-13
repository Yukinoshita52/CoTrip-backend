package com.trip.web.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/place-types")
public class PlaceTypeController {

//    private final PlaceTypeService placeTypeService;
//
//    public PlaceTypeController(PlaceTypeService placeTypeService) {
//        this.placeTypeService = placeTypeService;
//    }
//
//    @GetMapping
//    @Operation(summary = "List place types with pagination")
//    public Result<Page<PlaceType>> listPlaceTypes(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<PlaceType> placeTypePage = placeTypeService.page(new Page<>(page, size));
//        return Result.ok(placeTypePage);
//    }
//
//    @PostMapping
//    @Operation(summary = "Create a new place type")
//    public Result<Void> createPlaceType(@RequestBody @Validated PlaceType placeType) {
//        placeTypeService.save(placeType);
//        return Result.ok();
//    }
//
//    @PutMapping("/{id}")
//    @Operation(summary = "Update an existing place type")
//    public Result<Void> updatePlaceType(@PathVariable Long id, @RequestBody @Validated PlaceType placeType) {
//        placeType.setId(id);
//        placeTypeService.updateById(placeType);
//        return Result.ok();
//    }
//
//    @DeleteMapping("/{id}")
//    @Operation(summary = "Delete a place type")
//    public Result<Void> deletePlaceType(@PathVariable Long id) {
//        placeTypeService.removeById(id);
//        return Result.ok();
//    }
}
