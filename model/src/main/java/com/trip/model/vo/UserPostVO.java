package com.trip.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * ClassName: UserPostVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/24 19:52
 * @Version 1.0
 */
@Data
public class UserPostVO {
    private Long postId;
    private String tripName;
    private List<String> coverImages;
    private Date createTime;
}
