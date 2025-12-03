package com.trip.model.dto;

import lombok.Data;

/**
 * ClassName: CommentCountDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/26 9:40
 * @Version 1.0
 */
@Data
public class CommentCountDTO {
    private Long postId;
    private Integer commentCount;
}
