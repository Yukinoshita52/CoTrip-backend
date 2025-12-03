package com.trip.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.model.entity.Book;
import com.trip.web.service.BookService;

import com.trip.web.mapper.BookMapper;
import org.springframework.stereotype.Service;

/**
* @author 26423
* @description 针对表【book】的数据库操作Service实现
* @createDate 2025-11-29 15:05:15
*/
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book>
    implements BookService{

}




