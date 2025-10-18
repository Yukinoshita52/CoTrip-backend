package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.GraphInfo;
import com.trip.web.service.GraphInfoService;
import com.trip.web.mapper.GraphInfoMapper;
import org.springframework.stereotype.Service;
import com.trip.common.minio.MinioProperties;
import com.trip.web.service.UserService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
* @author 26423
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public String uploadImage(MultipartFile file, int itemType, Long itemId) {
        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String objectName = "images/" + UUID.randomUUID() + ext;

            try (InputStream is = file.getInputStream()) {
                PutObjectArgs args = PutObjectArgs.builder()
                        .bucket(StringUtils.hasText(properties.getBucketName()) ? properties.getBucketName() : "cotrip")
                        .object(objectName)
                        .contentType(file.getContentType())
                        .stream(is, file.getSize(), -1)
                        .build();
                minioClient.putObject(args);
            }

            String url = properties.getEndpoint() + "/" + (StringUtils.hasText(properties.getBucketName()) ? properties.getBucketName() : "cotrip") + "/" + objectName;

            GraphInfo gi = new GraphInfo();
            gi.setName(original);
            gi.setItemType(itemType);
            gi.setItemId(itemId);

            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    public String getImageUrlById(Long id) {
        GraphInfo graphInfo = this.getById(id);
        if (graphInfo == null) {
            //throw new RuntimeException("Image not found for ID: " + id);
            return "url/to/default/avatar.png";
        }
        return graphInfo.getUrl();
    }
}
