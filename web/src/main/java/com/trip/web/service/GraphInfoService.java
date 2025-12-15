package com.trip.web.service;

import com.trip.model.entity.GraphInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 26423
* @description 针对表【graph_info(图片信息表)】的数据库操作Service
* @createDate 2025-10-05 23:38:16
*/
public interface GraphInfoService extends IService<GraphInfo> {

    Long uploadImage(MultipartFile file, int itemType, Long itemId);
    String getImageUrlById(Long id);
    void deleteImageById(Long oldGraphId);
    
    /**
     * 获取行程封面图片URL
     * @param tripId 行程ID
     * @return 封面图片URL，如果没有则返回null
     */
    String getTripCoverImageUrl(Long tripId);
    
    /**
     * 设置行程封面图片
     * @param tripId 行程ID
     * @param imageUrl 图片URL
     * @return 图片记录ID
     */
    Long setTripCoverImage(Long tripId, String imageUrl);
    
    /**
     * 删除行程封面图片
     * @param tripId 行程ID
     */
    void deleteTripCoverImage(Long tripId);
}
