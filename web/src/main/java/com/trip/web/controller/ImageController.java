package com.trip.web.controller;

import com.trip.common.result.Result;
import com.trip.model.vo.ImageUrlVO;
import com.trip.web.service.GraphInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片管理控制器
 * 提供图片上传、获取、删除等功能
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final GraphInfoService graphInfoService;

    /**
     * 上传图片
     * @param file 图片文件
     * @param itemType 图片类型：1-用户头像，2-行程图片，3-地点图片，4-其他
     * @param itemId 关联对象ID（可选，用于关联特定对象）
     * @return 图片信息
     */
    @PostMapping("/upload")
    public Result<ImageUrlVO> uploadImage(@RequestPart("file") MultipartFile file,
                                          @RequestParam(value = "itemType", defaultValue = "4") Integer itemType,
                                          @RequestParam(value = "itemId", defaultValue = "0") Long itemId) {
        Long graphId = graphInfoService.uploadImage(file, itemType, itemId);
        String imageUrl = graphInfoService.getImageUrlById(graphId);
        
        ImageUrlVO vo = new ImageUrlVO();
        vo.setId(graphId);
        vo.setUrl(imageUrl);
        
        return Result.ok(vo);
    }

    /**
     * 获取图片URL
     * @param imageId 图片ID
     * @return 图片URL
     */
    @GetMapping("/{imageId}")
    public Result<ImageUrlVO> getImageUrl(@PathVariable Long imageId) {
        String imageUrl = graphInfoService.getImageUrlById(imageId);
        
        ImageUrlVO vo = new ImageUrlVO();
        vo.setId(imageId);
        vo.setUrl(imageUrl);
        
        return Result.ok(vo);
    }

    /**
     * 删除图片
     * @param imageId 图片ID
     * @return 操作结果
     */
    @DeleteMapping("/{imageId}")
    public Result<Void> deleteImage(@PathVariable Long imageId) {
        graphInfoService.deleteImageById(imageId);
        return Result.ok();
    }

    /**
     * 上传行程封面图片
     * @param file 图片文件
     * @param tripId 行程ID
     * @return 图片信息
     */
    @PostMapping("/trip/{tripId}/cover")
    public Result<ImageUrlVO> uploadTripCover(@RequestPart("file") MultipartFile file,
                                             @PathVariable Long tripId) {
        // 上传图片到MinIO
        Long graphId = graphInfoService.uploadImage(file, 2, tripId);
        String imageUrl = graphInfoService.getImageUrlById(graphId);
        
        // 设置为行程封面
        graphInfoService.setTripCoverImage(tripId, imageUrl);
        
        ImageUrlVO vo = new ImageUrlVO();
        vo.setId(graphId);
        vo.setUrl(imageUrl);
        
        return Result.ok(vo);
    }

    /**
     * 获取行程封面图片
     * @param tripId 行程ID
     * @return 图片URL
     */
    @GetMapping("/trip/{tripId}/cover")
    public Result<ImageUrlVO> getTripCover(@PathVariable Long tripId) {
        String imageUrl = graphInfoService.getTripCoverImageUrl(tripId);
        
        ImageUrlVO vo = new ImageUrlVO();
        vo.setUrl(imageUrl);
        
        return Result.ok(vo);
    }

    /**
     * 删除行程封面图片
     * @param tripId 行程ID
     * @return 操作结果
     */
    @DeleteMapping("/trip/{tripId}/cover")
    public Result<Void> deleteTripCover(@PathVariable Long tripId) {
        graphInfoService.deleteTripCoverImage(tripId);
        return Result.ok();
    }
}