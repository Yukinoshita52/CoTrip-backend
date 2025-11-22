package com.trip.model.dto;

import lombok.Data;

/**
 * ClassName: CommentDTO
 * Package: com.trip.model.dto
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/21 20:10
 * @Version 1.0
 */
@Data
public class CommentDTO {
    private Long postId;
    private String content;
    private Long parentId;
}
