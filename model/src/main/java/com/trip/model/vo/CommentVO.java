package com.trip.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * ClassName: CommentVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/23 14:52
 * @Version 1.0
 */
@Data
public class CommentVO {
    private Long commentId;
    private AuthorVO user;
    private String content;
    private Date createTime;

    @JsonIgnore //返回前端时不显示
    private Long parentId;

    private List<CommentVO> children;//子评论
}
