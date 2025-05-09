package com.gy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy.constant.UserConstant;
import com.gy.mapper.UserMapper;
import com.gy.model.entity.User;
import com.gy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;



/**
 * @author yun.guo
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-04-22 17:53:11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    }
}




