package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.InvitationCreateDTO;
import com.trip.model.dto.PlaceBatchImportDTO;
import com.trip.model.vo.InvitationVO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.web.service.InvitationService;
import com.trip.web.service.TripPlaceService;
import com.trip.web.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    private final TripPlaceService tripPlaceService;
    private final InvitationService invitationService;

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

    /**
     * 发出邀请（邀请他人加入行程）
     * @param tripId 行程ID
     * @param dto 邀请信息
     * @return 邀请详情
     */
    @PostMapping("/invitations")
    public Result<InvitationVO> createInvitation(
            @PathVariable Long tripId,
            @RequestBody @Validated InvitationCreateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        InvitationVO invitation = invitationService.createInvitation(tripId, loginUser.getUserId(), dto.getInvitee());
        return Result.ok(invitation);
    }

    /**
     * 查看该行程的邀请列表
     * @param tripId 行程ID
     * @return 邀请列表
     */
    @GetMapping("/invitations")
    public Result<List<InvitationVO>> getTripInvitations(@PathVariable Long tripId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        List<InvitationVO> invitations = invitationService.getSentInvitations(loginUser.getUserId());
        return Result.ok(invitations);
    }
}
