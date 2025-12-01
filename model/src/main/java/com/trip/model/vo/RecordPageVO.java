package com.trip.model.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: RecordPageVO
 * Package: com.trip.model.vo
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/12/1 11:09
 * @Version 1.0
 */
@Data
public class RecordPageVO {
    private Integer page;
    private Integer size;
    private Integer total;
    private List<RecordVO> lists;
}
