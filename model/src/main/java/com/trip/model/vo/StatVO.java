package com.trip.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * ClassName: StatVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/22 20:14
 * @Version 1.0
 */
@Data
public class StatVO {
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;

    /**
     * 可选参数（如返回true，则说明当前用户喜欢该帖子）
     */
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)//该注解表示，只有不为NULL才会在json中出现
    private Boolean liked;
}
