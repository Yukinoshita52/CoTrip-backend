package com.yukino.lease.common.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: AliyunSMSProperties
 * Package: com.yukino.lease.common.sms
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/28 13:48
 * @Version 1.0
 */
@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSMSProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint;
}