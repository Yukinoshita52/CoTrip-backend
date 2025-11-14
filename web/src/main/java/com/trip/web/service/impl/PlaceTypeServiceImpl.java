package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.PlaceType;
import com.trip.web.config.LLMClient;
import com.trip.web.service.PlaceTypeService;
import com.trip.web.mapper.PlaceTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 26423
* @description 针对表【place_type(地点类型表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
public class PlaceTypeServiceImpl extends ServiceImpl<PlaceTypeMapper, PlaceType>
    implements PlaceTypeService{
    private final LLMClient llmClient;

    /**
     * 根据 place 名称，让 LLM 判断所属 typeId
     */
    public Integer determineTypeId(String placeName) {
        // 获取所有类型
        List<PlaceType> types = this.list();

        // 构造 LLM 提示语
        StringBuilder prompt = new StringBuilder("下面是地点类型列表：\n");
        for (PlaceType t : types) {
            prompt.append(t.getCode()).append(": ").append(t.getName()).append("\n");
        }
        prompt.append("请判断以下地点属于哪种类型，只返回编码，不要其他文字：\n");
        prompt.append("地点名称: ").append(placeName);

        // 调用 LLM
        String llmResult = llmClient.chat(prompt.toString());
        if (llmResult == null || llmResult.isEmpty()) {
            log.warn("LLM 未返回地点类型");
            return 0;
        }

        // 根据 code 匹配 typeId
        for (PlaceType t : types) {
            if (llmResult.trim().equalsIgnoreCase(t.getCode())) {
                return t.getId().intValue();
            }
        }

        log.warn("LLM 返回的类型编码未匹配到数据库 type");
        return 0;
    }

    public String getTypeNameById(Integer typeId){
        PlaceType placeType = this.getById(typeId);
        if(placeType != null){
            return placeType.getName();
        } else {
            return "未知类型";
        }
    }
}




