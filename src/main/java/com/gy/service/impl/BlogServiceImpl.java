package com.gy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy.mapper.BlogMapper;
import com.gy.model.Blog;
import com.gy.service.BlogService;
import org.springframework.stereotype.Service;

/**
 * @author yun.guo
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2025-04-22 17:52:31
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

}




