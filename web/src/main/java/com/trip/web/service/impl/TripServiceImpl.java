package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.PlaceCreateDTO;
import com.trip.model.dto.SuggestionDTO;
import com.trip.model.dto.TripCreateDTO;
import com.trip.model.entity.Trip;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.TripVO;
import com.trip.web.config.LLMClient;
import com.trip.web.mapper.TripMapper;
import com.trip.web.service.TripService;
import com.trip.web.service.TripUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
@RequiredArgsConstructor
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip>
        implements TripService{

    private final LLMClient llmClient;
    private final PlaceServiceImpl placeService;
    private final TripUserService tripUserService;

    @Override
    public TripVO createTrip(TripCreateDTO dto, Long creatorId) {
        // 创建行程
        Trip trip = new Trip();
        BeanUtils.copyProperties(dto, trip);
        this.save(trip);

        // 将创建者添加到行程用户关联表
        tripUserService.addCreator(trip.getId(), creatorId);

        // 转换为VO返回
        TripVO vo = new TripVO();
        vo.setTripId(trip.getId());
        vo.setName(trip.getName());
        vo.setStartDate(trip.getStartDate());
        vo.setEndDate(trip.getEndDate());
        vo.setDescription(trip.getDescription());
        vo.setRegion(trip.getRegion());
        vo.setCreatedTime(trip.getCreateTime());

        return vo;
    }

    @Override
    public List<PlaceCreateVO> batchImportPlaces(Long tripId, String text){
        // 调用 LLMClient 提取地点名列表
        String prompt = """
            你是一名旅游信息提取助手。请阅读以下内容，提取出文中提到的所有【景点或地点名称】，包括但不限于：
            - 旅游景点（如“西湖”、“故宫”）
            - 餐厅或美食店铺（如“海底捞”、“瑞幸咖啡”）
            - 酒店或民宿（如“莫干山裸心谷”、“全季酒店”）
            - 景区内特色交通工具（如“漓江游船”、“黄山索道”、“迪士尼观光车”）

            要求：
            1. 仅提取文中明确写出的具体名称，不要泛称（如“附近餐厅”、“一家民宿”、“打车”）。
            2. 排除高铁、飞机、地铁、公交、出租车、网约车等通用交通工具。
            3. 输出必须是纯 JSON 数组，格式为：["名称1", "名称2", "名称3"]
            4. 不要包含任何解释、序号、Markdown 或额外文字。
            5. 若未找到符合条件的名称，请输出空数组：[]

            内容如下：
            %s
        """.formatted(text);

        String response = llmClient.chat(prompt);
        if (response == null || response.isEmpty()) {
            log.warn("LLM 未返回解析结果");
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "导入地点失败");
        }

        List<String> placeNames = new ArrayList<>();
        try {
            JsonNode node = new ObjectMapper().readTree(response);
            if (node.isArray()) {
                for (JsonNode n : node) {
                    placeNames.add(n.asText());
                }
            } else {
                Pattern pattern = Pattern.compile("\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                while (matcher.find()) {
                    placeNames.add(matcher.group(1));
                }
            }
        } catch (Exception e) {
            log.warn("解析 LLM 返回内容失败");
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "导入地点失败");
        }

        List<PlaceCreateVO> addedPlaces = new ArrayList<>();

        // 遍历每个地点名，自动补全 + 添加
        for (String name : placeNames) {
            try {
                // 获取输入提示（取第一个 suggestion）
                List<SuggestionDTO> suggestions = placeService.getSuggestions(name, tripId);
                if (suggestions.isEmpty()) {
                    log.warn("未找到匹配地点");
                    continue;
                }
                SuggestionDTO suggestion = suggestions.get(0);

                // 构造 PlaceCreateDTO
                PlaceCreateDTO createDTO = new PlaceCreateDTO();
                createDTO.setUid(suggestion.getUid());
                createDTO.setDay(0); // 默认待规划

                // 调用 addPlace
                PlaceCreateVO addResult = placeService.addPlace(tripId, createDTO);
                if (addResult != null) {
                    addedPlaces.add(addResult);
                } else {
                    log.warn("添加地点失败");
                }

            } catch (Exception e) {
                log.error("批量导入单条失败");
            }
        }

        // 返回结果
        if (addedPlaces.isEmpty()) {
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "导入地点失败");
        }
        return addedPlaces;
    }
}




