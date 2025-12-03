package com.trip.model.dto;

import lombok.Data;

/**
 * ClassName: LikeCountDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/26 9:41
 * @Version 1.0
 */
@Data
public class LikeCountDTO {
    private Long postId;
    private Integer likeCount;
}
