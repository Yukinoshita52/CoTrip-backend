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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
* @author 26423
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public Long uploadImage(MultipartFile file, int itemType, Long itemId) {
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

            return gi.getId();
        } catch (Exception e) {
            throw new LeaseException(ResultCodeEnum.IMAGE_UPLOAD_ERROR.getCode(), ResultCodeEnum.IMAGE_UPLOAD_ERROR.getMessage());
        }
    }

    @Override
    public String getImageUrlById(Long graphId) {
        if (graphId == null) {
            return null;
        }
        
        GraphInfo graph = this.getById(graphId);
        if (graph == null) {
            log.warn("未找到对应的图片信息，graphId: " + graphId);
            return null;
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
            log.error("获取 MinIO 预签名 URL 失败", e);
            return null;
        }
    }

    public void deleteImageById(Long graphId) {
        GraphInfo graph = this.getById(graphId);
        if (graph == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "未找到对应的图片信息");
        }

        graph.setIsDeleted((byte) 1);
        this.updateById(graph);

        String bucket = StringUtils.hasText(properties.getBucketName()) ? properties.getBucketName() : "cotrip";
        try {
            // 删除 MinIO 对象
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(graph.getUrl())
                            .build()
            );
        } catch (Exception e) {
            log.error("删除 MinIO 对象失败");
        }
    }

    @Override
    public String getTripCoverImageUrl(Long tripId) {
        // 查询行程封面图片 (itemType=2表示行程图片)
        GraphInfo graphInfo = this.lambdaQuery()
                .eq(GraphInfo::getItemType, 2)
                .eq(GraphInfo::getItemId, tripId)
                .eq(GraphInfo::getIsDeleted, 0)
                .orderByDesc(GraphInfo::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        if (graphInfo == null) {
            return null;
        }
        
        // 检查存储的是完整URL还是对象路径
        String storedUrl = graphInfo.getUrl();
        if (storedUrl.startsWith("http")) {
            // 如果是完整URL，直接返回
            return storedUrl;
        } else {
            // 如果是对象路径，生成预签名URL
            return getImageUrlById(graphInfo.getId());
        }
    }

    @Override
    public Long setTripCoverImage(Long tripId, String imageUrl) {
        // 先删除旧的封面图片
        deleteTripCoverImage(tripId);
        
        // 创建新的图片记录，直接存储URL
        GraphInfo gi = new GraphInfo();
        gi.setName("trip_cover_" + tripId);
        gi.setItemType(2); // 行程图片
        gi.setItemId(tripId);
        gi.setUrl(imageUrl); // 直接存储URL
        this.save(gi);
        
        return gi.getId();
    }

    @Override
    public void deleteTripCoverImage(Long tripId) {
        // 软删除所有该行程的封面图片
        this.lambdaUpdate()
                .eq(GraphInfo::getItemType, 2)
                .eq(GraphInfo::getItemId, tripId)
                .eq(GraphInfo::getIsDeleted, 0)
                .set(GraphInfo::getIsDeleted, 1)
                .update();
    }
}
