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
    /**
     * Upload an image and associate it with an item.
     * @param file the image file to upload
     * @param itemType the type of the item
     * @param itemId the ID of the item
     * @return the URL of the uploaded image
     */
    String uploadImage(MultipartFile file, int itemType, Long itemId);

    /**
     * Get the URL of an image by its ID.
     * @param id the ID of the image
     * @return the URL of the image
     */
    String getImageUrlById(Long id);
}
