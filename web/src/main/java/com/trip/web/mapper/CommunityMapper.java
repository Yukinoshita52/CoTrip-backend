package com.trip.web.mapper;

import com.trip.model.vo.SearchPostVO;

import java.util.List;

/**
 * ClassName: CommunityMapper
 * Package: com.trip.web.mapper
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/11/23 20:16
 * @Version 1.0
 */
public interface CommunityMapper {

    List<SearchPostVO.SearchItemVO> getPostsByKeyWord(String keyword);
}
