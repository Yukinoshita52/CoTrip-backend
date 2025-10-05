package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.GraphInfo;
import com.trip.web.service.GraphInfoService;
import com.trip.web.mapper.GraphInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2025-10-05 23:38:16
*/
@Service
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

}




