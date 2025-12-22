package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.PlaceType;
import com.trip.web.config.LLMClient;
import com.trip.web.service.LLMCacheService;
import com.trip.web.service.PlaceTypeService;
import com.trip.web.mapper.PlaceTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 26423
* @description 针对表【place_type(地点类型表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceTypeServiceImpl extends ServiceImpl<PlaceTypeMapper, PlaceType>
    implements PlaceTypeService{
    private final LLMClient llmClient;
    private final LLMCacheService llmCacheService;

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

        // 生成缓存键
        String cacheKey = llmCacheService.generateCacheKey("place_type", prompt.toString());
        
        // 尝试从缓存获取LLM响应
        String llmResult = llmCacheService.getLLMResponse(cacheKey);
        
        if (llmResult == null) {
            // 缓存未命中，调用LLM
            log.info("地点类型判断缓存未命中，调用LLM: placeName={}, cacheKey={}", placeName, cacheKey);
            
            llmResult = llmClient.chat(prompt.toString());
            if (llmResult == null || llmResult.isEmpty()) {
                log.warn("LLM 未返回地点类型");
                return 0;
            }
            
            // 缓存LLM响应
            llmCacheService.cacheLLMResponse(cacheKey, llmResult);
            log.info("地点类型判断LLM调用完成并已缓存: placeName={}, result={}, cacheKey={}", placeName, llmResult, cacheKey);
        } else {
            log.info("使用缓存的地点类型判断结果: placeName={}, result={}, cacheKey={}", placeName, llmResult, cacheKey);
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




