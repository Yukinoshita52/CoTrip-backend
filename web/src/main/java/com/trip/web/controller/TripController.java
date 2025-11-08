package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.model.dto.PlaceBatchImportDTO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.web.service.TripPlaceService;
import com.trip.web.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    private final TripPlaceService tripPlaceService;

//    // 获取行程中的所有地点 & 交通
//    @GetMapping("/itinerary")

//    // 调整地点顺序
//    @PutMapping("/places/order")
//    public Result<Void> updatePlaceOrder(
//            @PathVariable Long tripId,
//            @RequestBody List<Long> placeIds) {
//        tripPlaceService.updatePlaceOrder(tripId, placeIds);
//        return Result.ok();
//    }

    // 批量导入地点
    @PostMapping("/places/import")
    public Result<List<PlaceCreateVO>> importPlacesByText(
            @PathVariable Long tripId,
            @RequestBody PlaceBatchImportDTO dto) {
        return Result.ok(tripService.batchImportPlaces(tripId, dto.getText()));
    }

    // 删除地点
    @DeleteMapping("/places/{placeId}")
    public Result<Void> deletePlace(@PathVariable Long tripId, @PathVariable Long placeId) {
        tripPlaceService.deletePlace(tripId, placeId);
        return Result.ok();
    }
}
