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

    String uploadImage(MultipartFile file, int itemType, Long itemId);
    String getImageUrlById(Long id);
}
