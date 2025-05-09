package com.gy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy.common.ErrorCode;
import com.gy.constant.ThumbConstant;
import com.gy.exception.BusinessException;
import com.gy.manager.cache.CacheManager;
import com.gy.mapper.ThumbMapper;
import com.gy.model.dto.thumb.DoThumbRequest;
import com.gy.model.entity.Blog;
import com.gy.model.entity.Thumb;
import com.gy.model.entity.User;
import com.gy.service.BlogService;
import com.gy.service.ThumbService;
import com.gy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;


import java.io.Serializable;

/**
 * @author yun.guo
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-22 17:53:00
 */
@Service("thumbService")
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {
    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    private final CacheManager cacheManager;

    public ThumbServiceImpl(UserService userService, BlogService blogService, TransactionTemplate transactionTemplate, RedisTemplate<String, Object> redisTemplate, CacheManager cacheManager) {
        this.userService = userService;
        this.blogService = blogService;
        this.transactionTemplate = transactionTemplate;
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

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
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                //通过redis查询 改用户是否点赞过改博客
                boolean exists = this.hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户已点赞");
                }

                //更新博客点赞数
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                boolean success = update && this.save(thumb);
                // 更新Redis和本地缓存点赞记录
                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, fieldKey, realThumbId);
                    cacheManager.putIfPresent(hashKey, fieldKey, realThumbId);
                }
                // 更新成功才执行
                return success;
            });
        }
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
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                if (thumbIdObj == null || thumbIdObj.equals(ThumbConstant.UN_THUMB_CONSTANT)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean success = update && this.removeById((Serializable) thumbIdObj);
                // 点赞记录从 Redis 删除
                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hashKey, fieldKey);
                    cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
                }
                return success;
            });
        }
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
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = (Long) thumbIdObj;
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }

}




