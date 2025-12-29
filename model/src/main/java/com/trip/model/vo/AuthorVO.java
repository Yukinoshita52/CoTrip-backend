package com.trip.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * ClassName: Author
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/22 19:52
 * @Version 1.0
 */
@Data
public class AuthorVO {
    private Long userId;
    private String nickname;
    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String avatar;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String avatarUrl;
}
