package com.gy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author yun.guo
 * @description 针对表【user】的数据库操作Service
 * @createDate 2025-04-22 17:53:11
 */
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
}
