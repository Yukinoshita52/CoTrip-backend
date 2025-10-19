package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.entity.GraphInfo;
import com.trip.web.service.GraphInfoService;
import com.trip.web.mapper.GraphInfoMapper;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import com.trip.common.minio.MinioProperties;
import com.trip.web.service.UserService;
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
            String bucket = StringUtils.hasText(properties.getBucketName()) ? properties.getBucketName() : "cotrip";

            // 创建存储桶（如果不存在）
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // 上传对象（流式）
            try (InputStream is = file.getInputStream()) {
                PutObjectArgs args = PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .contentType(file.getContentType())
                        .stream(is, file.getSize(), -1)
                        .build();
                minioClient.putObject(args);
            }

            // 保存到 DB
            GraphInfo gi = new GraphInfo();
            gi.setName(original);
            gi.setItemType(itemType);
            gi.setItemId(itemId);
            gi.setUrl(objectName);
            this.save(gi);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(3600) // 一小时有效
                            .build()
            );
        } catch (Exception e) {
            throw new LeaseException(ResultCodeEnum.IMAGE_UPLOAD_ERROR.getCode(), ResultCodeEnum.IMAGE_UPLOAD_ERROR.getMessage());
        }
    }

    @Override
    public String getImageUrlById(Long graphId) {
        GraphInfo graph = this.getById(graphId);
        if (graph == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "未找到对应的图片信息");
        }

        String bucket = StringUtils.hasText(properties.getBucketName()) ? properties.getBucketName() : "cotrip";
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(graph.getUrl())
                            .expiry(60 * 60) // 有效期1小时
                            .build()
            );
        } catch (Exception e) {
            throw new LeaseException(ResultCodeEnum.IMAGE_DOWNLOAD_ERROR.getCode(), ResultCodeEnum.IMAGE_DOWNLOAD_ERROR.getMessage());
        }
    }
}
