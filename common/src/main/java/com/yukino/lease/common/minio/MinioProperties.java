package com.yukino.lease.common.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: MinioProperties
 * Package: com.yukino.lease.common.minio
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/17 14:09
 * @Version 1.0
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
