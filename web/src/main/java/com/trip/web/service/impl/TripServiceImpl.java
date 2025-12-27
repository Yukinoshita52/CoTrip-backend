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
import com.trip.model.entity.*;
import com.trip.model.vo.DayPlacesVO;
import com.trip.model.vo.PlaceCreateVO;
import com.trip.model.vo.PlaceInTripVO;
import com.trip.model.vo.TripDetailVO;
import com.trip.model.vo.TripMemberVO;
import com.trip.model.vo.TripVO;
import com.trip.web.config.LLMClient;
import com.trip.web.mapper.*;
import com.trip.web.service.GraphInfoService;
import com.trip.web.service.LLMCacheService;
import com.trip.web.service.PlaceTypeService;
import com.trip.web.service.RoutePlanCacheService;
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
    private final RoutePlanCacheService routePlanCacheService;
    private final LLMCacheService llmCacheService;
    
    // 添加必要的Mapper依赖
    private final BookMapper bookMapper;
    private final BookUserMapper bookUserMapper;
    private final AccountBookRecordMapper accountBookRecordMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    private final InvitationMapper invitationMapper;
    private final GraphInfoMapper graphInfoMapper;

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
    public List<PlaceCreateVO> batchImportPlaces(Long tripId, String text, Long userId){
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权导入地点");
        }
        
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

        // 生成缓存键
        String cacheKey = llmCacheService.generateCacheKey("place_import", text);
        
        // 尝试从缓存获取LLM响应
        String response = llmCacheService.getLLMResponse(cacheKey);
        
        if (response == null) {
            // 缓存未命中，调用LLM
            log.info("地点导入缓存未命中，调用LLM: cacheKey={}", cacheKey);
            
            response = llmClient.chat(prompt);
            if (response == null || response.isEmpty()) {
                log.warn("LLM 未返回解析结果");
                throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "导入地点失败");
            }
            
            // 缓存LLM响应
            llmCacheService.cacheLLMResponse(cacheKey, response);
            log.info("地点导入LLM调用完成并已缓存: cacheKey={}", cacheKey);
        } else {
            log.info("使用缓存的地点导入结果: cacheKey={}", cacheKey);
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

                // 调用 addPlace（批量导入时不需要再次检查权限，因为已经在方法开始时检查了）
                PlaceCreateVO addResult = placeService.addPlace(tripId, createDTO, userId);
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
        // 验证用户权限（必须是行程的创建者或参与者，且未退出行程）
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0)); // 添加这个条件确保用户未退出行程
        if (tripUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权删除此行程");
        }

        // 1. 先删除账本相关数据（按依赖关系顺序）
        // 1.1 删除账本记录
        List<Book> tripBooks = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getTripId, tripId)
                .eq(Book::getIsDeleted, 0));
        for (Book book : tripBooks) {
            // 删除账本记录
            UpdateWrapper<AccountBookRecord> recordUpdateWrapper = new UpdateWrapper<>();
            recordUpdateWrapper.eq("book_id", book.getId())
                    .set("is_deleted", 1);
            accountBookRecordMapper.update(null, recordUpdateWrapper);
            
            // 删除账本用户关系
            UpdateWrapper<BookUser> bookUserUpdateWrapper = new UpdateWrapper<>();
            bookUserUpdateWrapper.eq("book_id", book.getId())
                    .set("is_deleted", 1);
            bookUserMapper.update(null, bookUserUpdateWrapper);
        }
        
        // 1.2 删除账本
        for (Book book : tripBooks) {
            UpdateWrapper<Book> bookUpdateWrapper = new UpdateWrapper<>();
            bookUpdateWrapper.eq("id", book.getId())
                    .set("is_deleted", 1);
            bookMapper.update(null, bookUpdateWrapper);
        }

        // 2. 删除帖子相关数据
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getTripId, tripId)
                .eq(Post::getIsDeleted, 0));
        for (Post post : posts) {
            // 删除帖子评论
            UpdateWrapper<Comment> commentUpdateWrapper = new UpdateWrapper<>();
            commentUpdateWrapper.eq("post_id", post.getId())
                    .set("is_deleted", 1);
            commentMapper.update(null, commentUpdateWrapper);
            
            // 删除帖子点赞
            UpdateWrapper<PostLike> likeUpdateWrapper = new UpdateWrapper<>();
            likeUpdateWrapper.eq("post_id", post.getId())
                    .set("is_deleted", 1);
            postLikeMapper.update(null, likeUpdateWrapper);
        }
        
        // 删除帖子
        for (Post post : posts) {
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", post.getId())
                    .set("is_deleted", 1);
            postMapper.update(null, updateWrapper);
        }

        // 3. 删除邀请记录
        UpdateWrapper<Invitation> invitationUpdateWrapper = new UpdateWrapper<>();
        invitationUpdateWrapper.eq("trip_id", tripId)
                .set("is_deleted", 1);
        invitationMapper.update(null, invitationUpdateWrapper);

        // 4. 删除行程图片
        UpdateWrapper<GraphInfo> graphUpdateWrapper = new UpdateWrapper<>();
        graphUpdateWrapper.eq("item_type", 2) // 2表示行程图片
                .eq("item_id", tripId)
                .set("is_deleted", 1);
        graphInfoMapper.update(null, graphUpdateWrapper);

        // 5. 删除行程-地点关系
        List<TripPlace> tripPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId));
        for (TripPlace tp : tripPlaces) {
            UpdateWrapper<TripPlace> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", tp.getId())
                    .set("is_deleted", 1);
            tripPlaceMapper.update(null, updateWrapper);
        }

        // 6. 删除行程-用户关系（包括已退出的用户）
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId));
        for (TripUser tu : tripUsers) {
            UpdateWrapper<TripUser> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", tu.getId())
                    .set("is_deleted", 1);
            tripUserService.update(null, updateWrapper);
        }

        // 7. 最后删除行程
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }
        this.removeById(tripId);
    }

    @Override
    public TripVO updateTrip(Long tripId, TripUpdateDTO dto, Long userId) {
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
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
        // 查询用户关联的所有有效行程（排除已删除的关联关系）
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0) // 添加这个条件来排除已退出的行程
                .orderByDesc(TripUser::getCreateTime));

        // 批量查询所有行程，避免N+1查询
        List<Long> tripIds = tripUsers.stream()
                .map(TripUser::getTripId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Trip> tripMap = new java.util.HashMap<>();
        if (!tripIds.isEmpty()) {
            List<Trip> trips = this.listByIds(tripIds);
            tripMap = trips.stream()
                    .collect(Collectors.toMap(Trip::getId, t -> t));
        }

        // 批量查询所有行程的成员，避免N+1查询
        List<TripUser> allMembersTripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .in(TripUser::getTripId, tripIds)
                .eq(TripUser::getIsDeleted, 0)
                .orderByAsc(TripUser::getTripId)
                .orderByAsc(TripUser::getRole)
                .orderByAsc(TripUser::getCreateTime));
        Map<Long, List<TripUser>> membersByTripId = allMembersTripUsers.stream()
                .collect(Collectors.groupingBy(TripUser::getTripId));

        // 批量查询所有用户，避免N+1查询
        List<Long> allUserIds = allMembersTripUsers.stream()
                .map(TripUser::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = new java.util.HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userService.listByIds(allUserIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        List<TripVO> tripVOList = new ArrayList<>();
        for (TripUser tu : tripUsers) {
            Trip trip = tripMap.get(tu.getTripId());
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

                // 获取成员列表（排除已退出的成员）
                List<TripUser> membersTripUsers = membersByTripId.getOrDefault(trip.getId(), new ArrayList<>());

                List<TripMemberVO> members = new ArrayList<>();
                for (TripUser memberTu : membersTripUsers) {
                    User user = userMap.get(memberTu.getUserId());
                    if (user != null) {
                        TripMemberVO memberVO = new TripMemberVO();
                        memberVO.setUserId(user.getId());
                        memberVO.setUsername(user.getUsername());
                        memberVO.setNickname(user.getNickname());
                        memberVO.setAvatarUrl(graphInfoService.getImageUrlById(user.getAvatarId()));
                        memberVO.setRole(memberTu.getRole());
                        memberVO.setJoinedAt(memberTu.getCreateTime());
                        members.add(memberVO);
                    }
                }
                vo.setMembers(members);

                tripVOList.add(vo);
            }
        }

        return tripVOList;
    }

    @Override
    public TripDetailVO getTripDetail(Long tripId, Long userId) {
        // 验证用户权限（确保用户仍在行程中，未退出）
        TripUser tripUser = tripUserService.getOne(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getUserId, userId)
                .eq(TripUser::getIsDeleted, 0)); // 添加这个条件确保用户未退出行程
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

        // 批量查询所有地点，避免N+1查询
        List<Long> placeIds = tripPlaces.stream()
                .map(TripPlace::getPlaceId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Place> placeMap = new java.util.HashMap<>();
        if (!placeIds.isEmpty()) {
            List<Place> places = placeMapper.selectBatchIds(placeIds);
            placeMap = places.stream()
                    .collect(Collectors.toMap(Place::getId, p -> p));
        }

        // 批量查询所有地点类型，避免N+1查询
        List<Integer> typeIds = placeMap.values().stream()
                .map(Place::getTypeId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> typeNameMap = new java.util.HashMap<>();
        if (!typeIds.isEmpty()) {
            for (Integer typeId : typeIds) {
                String typeName = placeTypeService.getTypeNameById(typeId);
                if (typeName != null) {
                    typeNameMap.put(typeId, typeName);
                }
            }
        }

        // 按天数分组
        Map<Integer, List<TripPlace>> placesByDay = tripPlaces.stream()
                .collect(Collectors.groupingBy(TripPlace::getDay));

        List<DayPlacesVO> dayPlacesList = new ArrayList<>();
        for (Map.Entry<Integer, List<TripPlace>> entry : placesByDay.entrySet()) {
            DayPlacesVO dayPlacesVO = new DayPlacesVO();
            dayPlacesVO.setDay(entry.getKey());

            List<PlaceInTripVO> placeInTripList = new ArrayList<>();
            for (TripPlace tp : entry.getValue()) {
                Place place = placeMap.get(tp.getPlaceId());
                if (place != null) {
                    PlaceInTripVO placeInTripVO = new PlaceInTripVO();
                    placeInTripVO.setId(place.getId());
                    placeInTripVO.setName(place.getName());
                    placeInTripVO.setType(typeNameMap.getOrDefault(place.getTypeId(), ""));
                    placeInTripVO.setTypeId(place.getTypeId());
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

        // 获取成员列表（排除已退出的成员）
        List<TripUser> tripUsers = tripUserService.list(new LambdaQueryWrapper<TripUser>()
                .eq(TripUser::getTripId, tripId)
                .eq(TripUser::getIsDeleted, 0) // 添加这个条件来排除已退出的成员
                .orderByAsc(TripUser::getRole) // 创建者在前
                .orderByAsc(TripUser::getCreateTime)); // 按加入时间排序

        // 批量查询所有用户，避免N+1查询
        List<Long> userIds = tripUsers.stream()
                .map(TripUser::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        List<TripMemberVO> members = new ArrayList<>();
        for (TripUser tu : tripUsers) {
            User user = userMap.get(tu.getUserId());
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
        // 验证用户是否有编辑权限（创建者或管理员）
        if (!tripUserService.hasEditPermission(tripId, userId)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "无权规划此行程");
        }

        // 获取所有待规划的地点（day=0）
        List<TripPlace> unplannedPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId)
                .eq(TripPlace::getDay, 0));

        if (unplannedPlaces.isEmpty()) {
            return "没有待规划的地点";
        }

        // 获取所有已规划的地点（day>0）
        List<TripPlace> plannedPlaces = tripPlaceMapper.selectList(new LambdaQueryWrapper<TripPlace>()
                .eq(TripPlace::getTripId, tripId)
                .gt(TripPlace::getDay, 0)
                .orderByAsc(TripPlace::getDay)
                .orderByAsc(TripPlace::getSequence));

        // 获取行程信息
        Trip trip = this.getById(tripId);
        if (trip == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR.getCode(), "行程不存在");
        }

        // 计算行程天数
        int tripDays = 1;
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            long diffInMillies = trip.getEndDate().getTime() - trip.getStartDate().getTime();
            tripDays = Math.max(1, (int) (diffInMillies / (1000 * 60 * 60 * 24)) + 1);
        }

        // 构建已规划地点信息
        StringBuilder existingPlanInfo = new StringBuilder();
        Map<Integer, List<Place>> plannedPlacesByDay = new java.util.HashMap<>();

        for (TripPlace tp : plannedPlaces) {
            Place place = placeMapper.selectById(tp.getPlaceId());
            if (place != null) {
                plannedPlacesByDay.computeIfAbsent(tp.getDay(), k -> new ArrayList<>()).add(place);
            }
        }

        // 构建已规划地点的描述信息
        for (Map.Entry<Integer, List<Place>> entry : plannedPlacesByDay.entrySet()) {
            existingPlanInfo.append("第").append(entry.getKey()).append("天: ");
            List<Place> places = entry.getValue();
            for (int i = 0; i < places.size(); i++) {
                Place place = places.get(i);
                existingPlanInfo.append(place.getName())
                        .append("(").append(place.getLat()).append(",").append(place.getLng()).append(")");
                if (i < places.size() - 1) {
                    existingPlanInfo.append(" -> ");
                }
            }
            existingPlanInfo.append("; ");
        }

        // 构建待规划地点信息
        StringBuilder unplannedPlacesInfo = new StringBuilder();
        List<Long> unplannedPlaceIds = new ArrayList<>();
        for (TripPlace tp : unplannedPlaces) {
            Place place = placeMapper.selectById(tp.getPlaceId());
            if (place != null) {
                unplannedPlacesInfo.append(place.getName()).append("(")
                        .append(place.getLat()).append(",").append(place.getLng()).append(");");
                unplannedPlaceIds.add(place.getId());
            }
        }

        // 构建所有地点ID列表用于缓存键
        List<Long> allPlaceIds = new ArrayList<>();
        allPlaceIds.addAll(plannedPlaces.stream().map(TripPlace::getPlaceId).collect(Collectors.toList()));
        allPlaceIds.addAll(unplannedPlaceIds);

        // 生成缓存键（包含已规划地点信息）
        String cacheKey = routePlanCacheService.generateCacheKey(
                existingPlanInfo.toString() + unplannedPlacesInfo.toString(), tripDays);

        // 尝试从缓存获取路径规划结果
        String cachedRoutePlan = routePlanCacheService.getRoutePlan(cacheKey);
        String routePlanJson = null;
        
        if (cachedRoutePlan != null && routePlanCacheService.isValidRoutePlan(cachedRoutePlan, allPlaceIds)) {
            // 缓存命中且有效
            routePlanJson = cachedRoutePlan;
            log.info("使用缓存的路径规划结果: tripId={}, cacheKey={}", tripId, cacheKey);
        } else {
            // 缓存未命中或无效，调用LLM进行路线规划
            log.info("缓存未命中，调用LLM进行路径规划: tripId={}, cacheKey={}", tripId, cacheKey);
            
            String prompt;
            if (existingPlanInfo.length() > 0) {
                // 有已规划地点的情况
                prompt = String.format("""
                    请帮我优化一个旅行路线。以下是行程信息：
                    - 行程名称：%s
                    - 开始日期：%s
                    - 结束日期：%s
                    - 目的地：%s
                    - 行程天数：%d天
                    
                    当前已规划的行程安排：
                    %s
                    
                    以下是需要新增的地点列表（格式：地点名(纬度,经度)）：
                    %s
                    
                    请将新增地点合理地插入到现有行程中，可以调整原有地点的天数和顺序以优化整体路线。
                    返回完整的行程安排，包括原有地点和新增地点的JSON格式：
                    [
                      {"placeId": 1, "day": 1, "sequence": 1},
                      {"placeId": 2, "day": 1, "sequence": 2},
                      ...
                    ]
                    
                    注意：
                    1. day从1开始，表示第几天，最大不超过%d天
                    2. sequence表示同一天内的顺序，从1开始
                    3. 请考虑地理位置优化路线，避免过度绕路
                    4. 每天安排的地点数量要合理，不宜过多
                    5. 必须包含所有已规划和新增的地点
                    6. 可以重新调整原有地点的day和sequence来优化路线
                    """, trip.getName(), trip.getStartDate(), trip.getEndDate(), trip.getRegion(),
                    tripDays, existingPlanInfo.toString(), unplannedPlacesInfo.toString(), tripDays);
            } else {
                // 没有已规划地点的情况（原有逻辑）
                prompt = String.format("""
                    请帮我规划一个旅行路线。以下是行程信息：
                    - 行程名称：%s
                    - 开始日期：%s
                    - 结束日期：%s
                    - 目的地：%s
                    - 行程天数：%d天
                    
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
                    1. day从1开始，表示第几天，最大不超过%d天
                    2. sequence表示同一天内的顺序
                    3. 请合理分配，考虑地理位置和时间安排
                    4. 每个地点都必须分配到某一天
                    """, trip.getName(), trip.getStartDate(), trip.getEndDate(), trip.getRegion(),
                    tripDays, unplannedPlacesInfo.toString(), tripDays);
            }

            try {
                String response = llmClient.chat(prompt);
                if (response == null || response.isEmpty()) {
                    throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "路线规划失败");
                }
                
                // 提取JSON部分（去除可能的额外文本）
                routePlanJson = extractJsonFromResponse(response);
                
                // 验证JSON格式
                JsonNode testNode = new ObjectMapper().readTree(routePlanJson);
                if (!testNode.isArray()) {
                    throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "LLM返回的路线规划格式无效");
                }
                
                // 缓存结果
                routePlanCacheService.cacheRoutePlan(cacheKey, routePlanJson);
                log.info("LLM路径规划完成并已缓存: tripId={}, cacheKey={}", tripId, cacheKey);
                
            } catch (Exception e) {
                log.error("LLM路线规划失败: tripId={}", tripId, e);
                throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "路线规划失败：" + e.getMessage());
            }
        }

        // 应用路径规划结果
        try {
            JsonNode node = new ObjectMapper().readTree(routePlanJson);
            int updatedCount = 0;
            
            for (JsonNode item : node) {
                Long placeId = item.path("placeId").asLong();
                Integer day = item.path("day").asInt();
                Integer sequence = item.path("sequence").asInt();

                // 查找对应的TripPlace记录（不限制day=0，包含所有地点）
                TripPlace existingTp = tripPlaceMapper.selectOne(new LambdaQueryWrapper<TripPlace>()
                        .eq(TripPlace::getTripId, tripId)
                        .eq(TripPlace::getPlaceId, placeId));

                if (existingTp != null) {
                    // 检查是否需要更新
                    if (!day.equals(existingTp.getDay()) || !sequence.equals(existingTp.getSequence())) {
                        TripPlace updateTp = new TripPlace();
                        updateTp.setId(existingTp.getId());
                        updateTp.setDay(day);
                        updateTp.setSequence(sequence);
                        tripPlaceMapper.updateById(updateTp);
                        updatedCount++;
                    }
                } else {
                    log.warn("未找到placeId={}的TripPlace记录", placeId);
                }
            }
            
            log.info("路径规划应用完成: tripId={}, 更新了{}个地点", tripId, updatedCount);

            // 统计结果
            int totalUnplannedCount = unplannedPlaces.size();
            int totalPlannedCount = plannedPlaces.size();

            if (totalPlannedCount > 0) {
                return String.format("路线规划完成，已将%d个新地点插入现有行程，同时优化了原有%d个地点的安排",
                        totalUnplannedCount, totalPlannedCount);
            } else {
                return String.format("路线规划完成，共安排了%d个地点", totalUnplannedCount);
            }

        } catch (Exception e) {
            log.error("应用路径规划结果失败: tripId={}", tripId, e);
            throw new LeaseException(ResultCodeEnum.FAIL.getCode(), "应用路线规划失败：" + e.getMessage());
        }
    }

    /**
     * 从LLM响应中提取JSON部分
     * 
     * @param response LLM的完整响应
     * @return 提取的JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        // 查找JSON数组的开始和结束
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // 如果没找到数组格式，返回原始响应
        return response.trim();
    }
}




