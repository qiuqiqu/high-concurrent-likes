package com.gy.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy.common.ErrorCode;
import com.gy.constant.RedisLuaScriptConstant;
import com.gy.constant.ThumbConstant;
import com.gy.exception.BusinessException;
import com.gy.mapper.ThumbMapper;
import com.gy.model.dto.thumb.DoThumbRequest;
import com.gy.model.entity.LuaStatusEnum;
import com.gy.model.entity.Thumb;
import com.gy.model.entity.User;
import com.gy.service.BlogService;
import com.gy.service.ThumbService;
import com.gy.service.UserService;
import com.gy.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author yun.guo
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-22 17:53:00
 */
@Service("thumbServiceRedis")
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {
    @Autowired
    private  UserService userService;
    @Autowired
    private  BlogService blogService;
    @Autowired
    private  TransactionTemplate transactionTemplate;
    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;



    /**
     * 点赞
     *
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long blogId = doThumbRequest.getBlogId();

        String timeSlice = getTimeSlice();
        //Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        //执行脚本
        RedisScript<Long> thumbScript = RedisLuaScriptConstant.THUMB_SCRIPT;
        Long result = redisTemplate.execute(
                thumbScript,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new RuntimeException("用户已点赞");
        }

        //通过redis查询 改用户是否点赞过改博客
        boolean exists = this.hasThumb(blogId, loginUser.getId());
        if (exists) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户已点赞");
        }

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;

    }

    /**
     * 取消点赞
     *
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long blogId = doThumbRequest.getBlogId();
        // 计算时间片
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );
        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new RuntimeException("用户未点赞");
        }
        return LuaStatusEnum.SUCCESS.getValue() == result;

    }

    /**
     * 判断用户是否点赞
     *
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
    }

    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

}




