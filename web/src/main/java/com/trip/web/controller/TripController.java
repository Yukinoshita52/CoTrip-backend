package com.trip.web.controller;

import com.trip.common.login.LoginUserHolder;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.InvitationCreateDTO;
import com.trip.model.dto.PlaceBatchImportDTO;
import com.trip.model.dto.PlaceUpdateDTO;
import com.trip.model.dto.TripUpdateDTO;
import com.trip.model.vo.InvitationVO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.TripDetailVO;
import com.trip.model.vo.TripVO;
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

    /**
     * 查看行程详情（含地点列表）
     * @param tripId 行程ID
     * @return 行程详情
     */
    @GetMapping
    public Result<TripDetailVO> getTripDetail(@PathVariable Long tripId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        TripDetailVO detailVO = tripService.getTripDetail(tripId, loginUser.getUserId());
        return Result.ok(detailVO);
    }

    /**
     * 删除行程
     * @param tripId 行程ID
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> deleteTrip(@PathVariable Long tripId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        tripService.deleteTrip(tripId, loginUser.getUserId());
        return Result.ok();
    }

    /**
     * 修改行程信息
     * @param tripId 行程ID
     * @param dto 行程更新信息
     * @return 更新后的行程信息
     */
    @PutMapping
    public Result<TripVO> updateTrip(
            @PathVariable Long tripId,
            @RequestBody @Validated TripUpdateDTO dto) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        TripVO tripVO = tripService.updateTrip(tripId, dto, loginUser.getUserId());
        return Result.ok(tripVO);
    }

    /**
     * 编辑地点顺序
     * @param tripId 行程ID
     * @param placeIds 地点ID列表（按顺序排列）
     * @return 操作结果
     */
    @PutMapping("/places/order")
    public Result<Void> updatePlaceOrder(
            @PathVariable Long tripId,
            @RequestBody List<Long> placeIds) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        tripPlaceService.updatePlaceOrder(tripId, placeIds);
        return Result.ok();
    }

    /**
     * 一键规划行程路线
     * @param tripId 行程ID
     * @return 规划结果
     */
    @GetMapping("/auto-plan")
    public Result<String> autoPlanRoute(@PathVariable Long tripId) {
        var loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), ResultCodeEnum.APP_LOGIN_AUTH.getMessage());
        }
        
        String message = tripService.autoPlanRoute(tripId, loginUser.getUserId());
        return Result.ok(message);
    }

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

    // 更新地点信息
    @PutMapping("/places/{placeId}")
    public Result<Void> updatePlace(
            @PathVariable Long tripId, 
            @PathVariable Long placeId,
            @RequestBody @Validated PlaceUpdateDTO dto) {
        tripPlaceService.updatePlace(tripId, placeId, dto.getDay(), dto.getTypeId());
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
        // 获取该用户发出的所有邀请，然后过滤出该行程的邀请
        List<InvitationVO> allInvitations = invitationService.getSentInvitations(loginUser.getUserId());
        List<InvitationVO> tripInvitations = allInvitations.stream()
                .filter(inv -> inv.getTripId().equals(tripId))
                .collect(java.util.stream.Collectors.toList());
        return Result.ok(tripInvitations);
    }
}
