package com.trip.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.exception.LeaseException;
import com.trip.common.result.ResultCodeEnum;
import com.trip.model.dto.PlaceCreateDTO;
import com.trip.model.dto.SuggestionDTO;
import com.trip.model.dto.TripCreateDTO;
import com.trip.model.dto.TripUpdateDTO;
import com.trip.model.entity.Place;
import com.trip.model.entity.Post;
import com.trip.model.entity.Trip;
import com.trip.model.entity.TripPlace;
import com.trip.model.entity.TripUser;
import com.trip.model.entity.User;
import com.trip.model.vo.DayPlacesVO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.PlaceInTripVO;
import com.trip.model.vo.TripDetailVO;
import com.trip.model.vo.TripMemberVO;
import com.trip.model.vo.TripVO;
import com.trip.web.config.LLMClient;
import com.trip.web.mapper.TripMapper;
import com.trip.web.mapper.TripPlaceMapper;
import com.trip.web.mapper.PlaceMapper;
import com.trip.web.mapper.PostMapper;
import com.trip.web.service.GraphInfoService;
import com.trip.web.service.PlaceTypeService;
import com.trip.web.service.TripService;
import com.trip.web.service.TripUserService;
import com.trip.web.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author 26423
* @description 针对表【trip(行程表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl extends ServiceImpl<TripMapper, Trip>
        implements TripService{

    private final LLMClient llmClient;
    private final PlaceServiceImpl placeService;
    private final TripUserService tripUserService;
    private final TripPlaceMapper tripPlaceMapper;
    private final UserService userService;
    private final PlaceMapper placeMapper;
    private final PlaceTypeService placeTypeService;
    private final PostMapper postMapper;
    private final GraphInfoService graphInfoService;

    @Override
    public TripVO createTrip(TripCreateDTO dto, Long creatorId) {
        // 创建行程
        Trip trip = new Trip();
        BeanUtils.copyProperties(dto, trip, "coverImageUrl"); // 排除coverImageUrl字段
        this.save(trip);

        // 如果有封面图片URL，保存到图片表
        if (dto.getCoverImageUrl() != null && !dto.getCoverImageUrl().isEmpty()) {
            graphInfoService.setTripCoverImage(trip.getId(), dto.getCoverImageUrl());
        }

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
        vo.setCoverImageUrl(graphInfoService.getTripCoverImageUrl(trip.getId()));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTrip(Long tripId, Long userId) {
        // 验证用户权限（必须是行程的创建者或参与者）
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId));
        if (tripUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权删除此行程");
        }

        // 先查询并逻辑删除关联的行程-用户关系
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId));
        for (TripUser tu : tripUsers) {
            UpdateWrapper<TripUser> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", tu.getId())
                    .set("is_deleted", 1);
            tripUserService.update(null, updateWrapper);
        }

        // 逻辑删除关联的行程-地点关系
        List<TripPlace> tripPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId));
        for (TripPlace tp : tripPlaces) {
            UpdateWrapper<TripPlace> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", tp.getId())
                    .set("is_deleted", 1);
            tripPlaceMapper.update(null, updateWrapper);
        }

        // 逻辑删除关联的帖子
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getTripId, tripId));
        for (Post post : posts) {
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", post.getId())
                    .set("is_deleted", 1);
            postMapper.update(null, updateWrapper);
        }

        // 最后逻辑删除行程
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }
        this.removeById(tripId);
    }

    @Override
    public TripVO updateTrip(Long tripId, TripUpdateDTO dto, Long userId) {
        // 验证用户权限
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId));
        if (tripUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权修改此行程");
        }

        // 更新行程信息
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }

        if (dto.getName() != null) {
            trip.setName(dto.getName());
        }
        if (dto.getStartDate() != null) {
            trip.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            trip.setEndDate(dto.getEndDate());
        }
        if (dto.getDescription() != null) {
            trip.setDescription(dto.getDescription());
        }
        if (dto.getRegion() != null) {
            trip.setRegion(dto.getRegion());
        }
        
        // 处理封面图片更新
        if (dto.getCoverImageUrl() != null) {
            if (dto.getCoverImageUrl().isEmpty()) {
                // 如果传入空字符串，删除封面图片
                graphInfoService.deleteTripCoverImage(trip.getId());
            } else {
                // 更新封面图片
                graphInfoService.setTripCoverImage(trip.getId(), dto.getCoverImageUrl());
            }
        }

        this.updateById(trip);

        // 转换为VO返回
        TripVO vo = new TripVO();
        vo.setTripId(trip.getId());
        vo.setName(trip.getName());
        vo.setStartDate(trip.getStartDate());
        vo.setEndDate(trip.getEndDate());
        vo.setDescription(trip.getDescription());
        vo.setRegion(trip.getRegion());
        vo.setCoverImageUrl(graphInfoService.getTripCoverImageUrl(trip.getId()));
        vo.setCreatedTime(trip.getCreateTime());

        return vo;
    }

    @Override
    public List<TripVO> getUserTrips(Long userId) {
        // 查询用户关联的所有行程
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getUserId, userId)
                .orderByDesc(TripUser::getCreateTime));

        List<TripVO> tripVOList = new ArrayList<>();
        for (TripUser tu : tripUsers) {
            Trip trip = this.getById(tu.getTripId());
            if (trip != null) {
                TripVO vo = new TripVO();
                vo.setTripId(trip.getId());
                vo.setName(trip.getName());
                vo.setStartDate(trip.getStartDate());
                vo.setEndDate(trip.getEndDate());
                vo.setDescription(trip.getDescription());
                vo.setRegion(trip.getRegion());
                vo.setCoverImageUrl(graphInfoService.getTripCoverImageUrl(trip.getId()));
                vo.setCreatedTime(trip.getCreateTime());
                tripVOList.add(vo);
            }
        }

        return tripVOList;
    }

    @Override
    public TripDetailVO getTripDetail(Long tripId, Long userId) {
        // 验证用户权限
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId));
        if (tripUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权查看此行程");
        }

        // 获取行程基本信息
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }

        TripDetailVO detailVO = new TripDetailVO();
        detailVO.setTripId(trip.getId());
        detailVO.setName(trip.getName());
        detailVO.setStartDate(trip.getStartDate());
        detailVO.setEndDate(trip.getEndDate());
        detailVO.setDescription(trip.getDescription());
        detailVO.setRegion(trip.getRegion());
        detailVO.setCoverImageUrl(graphInfoService.getTripCoverImageUrl(trip.getId()));
        detailVO.setCreatedTime(trip.getCreateTime());

        // 获取地点列表，按天数分组
        List<TripPlace> tripPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId)
                .orderByAsc(TripPlace::getDay)
                .orderByAsc(TripPlace::getSequence));

        // 按天数分组
        Map<Integer, List<TripPlace>> placesByDay = tripPlaces.stream()
                .collect(Collectors.groupingBy(TripPlace::getDay));

        List<DayPlacesVO> dayPlacesList = new ArrayList<>();
        for (Map.Entry<Integer, List<TripPlace>> entry : placesByDay.entrySet()) {
            DayPlacesVO dayPlacesVO = new DayPlacesVO();
            dayPlacesVO.setDay(entry.getKey());

            List<PlaceInTripVO> placeInTripList = new ArrayList<>();
            for (TripPlace tp : entry.getValue()) {
                Place place = placeMapper.selectById(tp.getPlaceId());
                if (place != null) {
                    PlaceInTripVO placeInTripVO = new PlaceInTripVO();
                    placeInTripVO.setId(place.getId());
                    placeInTripVO.setName(place.getName());
                    placeInTripVO.setType(placeTypeService.getTypeNameById(place.getTypeId()));
                    placeInTripVO.setLat(place.getLat());
                    placeInTripVO.setLng(place.getLng());
                    placeInTripVO.setAddress(place.getAddress());
                    placeInTripVO.setSequence(tp.getSequence());
                    // 注意：Place实体中没有notes字段，如果需要可以从TripPlace扩展
                    placeInTripList.add(placeInTripVO);
                }
            }

            dayPlacesVO.setPlaces(placeInTripList);
            dayPlacesList.add(dayPlacesVO);
        }

        detailVO.setPlaces(dayPlacesList);

        // 获取成员列表
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .orderByAsc(TripUser::getRole) // 创建者在前
                .orderByAsc(TripUser::getCreateTime)); // 按加入时间排序

        List<TripMemberVO> members = new ArrayList<>();
        for (TripUser tu : tripUsers) {
            User user = userService.getById(tu.getUserId());
            if (user != null) {
                TripMemberVO memberVO = new TripMemberVO();
                memberVO.setUserId(user.getId());
                memberVO.setUsername(user.getUsername());
                memberVO.setNickname(user.getNickname());
                memberVO.setAvatarUrl(graphInfoService.getImageUrlById(user.getAvatarId()));
                memberVO.setRole(tu.getRole());
                memberVO.setJoinedAt(tu.getCreateTime());
                members.add(memberVO);
            }
        }
        detailVO.setMembers(members);

        return detailVO;
    }

    @Override
    public String autoPlanRoute(Long tripId, Long userId) {
        // 验证用户权限
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId));
        if (tripUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权规划此行程");
        }

        // 获取所有待规划的地点（day=0）
        List<TripPlace> unplannedPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId)
                .eq(TripPlace::getDay, 0));

        if (unplannedPlaces.isEmpty()) {
            return "没有待规划的地点";
        }

        // 获取行程信息
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }

        // 构建地点信息
        StringBuilder placesInfo = new StringBuilder();
        for (TripPlace tp : unplannedPlaces) {
            Place place = placeMapper.selectById(tp.getPlaceId());
            if (place != null) {
                placesInfo.append(place.getName()).append("(")
                        .append(place.getLat()).append(",").append(place.getLng()).append(");");
            }
        }

        // 调用LLM进行路线规划
        String prompt = String.format("""
            请帮我规划一个旅行路线。以下是行程信息：
            - 行程名称：%s
            - 开始日期：%s
            - 结束日期：%s
            - 目的地：%s
            
            以下是需要规划的地点列表（格式：地点名(纬度,经度)）：
            %s
            
            请根据这些地点，规划合理的行程安排，将每个地点分配到合适的天数（第1天、第2天等）。
            返回JSON格式，格式如下：
            [
              {"placeId": 1, "day": 1, "sequence": 1},
              {"placeId": 2, "day": 1, "sequence": 2},
              ...
            ]
            
            注意：
            1. day从1开始，表示第几天
            2. sequence表示同一天内的顺序
            3. 请合理分配，考虑地理位置和时间安排
            """, trip.getName(), trip.getStartDate(), trip.getEndDate(), trip.getRegion(), placesInfo.toString());

        try {
            String response = llmClient.chat(prompt);
            if (response == null || response.isEmpty()) {
                throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "路线规划失败");
            }

            // 解析JSON并更新地点分配
            JsonNode node = new ObjectMapper().readTree(response);
            if (node.isArray()) {
                for (JsonNode item : node) {
                    Long placeId = item.path("placeId").asLong();
                    Integer day = item.path("day").asInt();
                    Integer sequence = item.path("sequence").asInt();

                    // 查找对应的TripPlace记录
                    TripPlace existingTp = tripPlaceMapper.selectOne(new LambdaQueryWrapper<TripPlace>()
                            .eq(TripPlace::getTripId, tripId)
                            .eq(TripPlace::getPlaceId, placeId)
                            .eq(TripPlace::getDay, 0));
                    
                    if (existingTp != null) {
                        // 更新TripPlace
                        TripPlace updateTp = new TripPlace();
                        updateTp.setId(existingTp.getId());
                        updateTp.setDay(day);
                        updateTp.setSequence(sequence);
                        tripPlaceMapper.updateById(updateTp);
                    }
                }
            }

            return "路线规划完成";
        } catch (Exception e) {
            log.error("一键规划失败", e);
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "路线规划失败：" + e.getMessage());
        }
    }
}




