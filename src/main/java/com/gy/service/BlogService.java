package com.gy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy.model.entity.Blog;
import com.gy.model.entity.User;
import com.gy.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;


import java.util.List;

/**
 * @author yun.guo
 * @description 针对表【blog】的数据库操作Service
 * @createDate 2025-04-22 17:52:31
 */
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    BlogVO getBlogVO(Blog blog, User loginUser);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);

}
