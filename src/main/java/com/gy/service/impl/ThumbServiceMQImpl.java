package com.gy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gy.constant.RedisLuaScriptConstant;
import com.gy.listener.thumb.msg.ThumbEvent;
import com.gy.mapper.ThumbMapper;
import com.gy.model.dto.thumb.DoThumbRequest;
import com.gy.model.entity.LuaStatusEnum;
import com.gy.model.entity.Thumb;
import com.gy.model.entity.User;
import com.gy.service.ThumbService;
import com.gy.service.UserService;
import com.gy.util.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;



import java.time.LocalDateTime;
import java.util.Arrays;

@Service("thumbServiceMQ")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

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
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogId();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本，点赞存入 Redis
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
                Arrays.asList(userThumbKey),
                blogId
        );
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new RuntimeException("用户已点赞");
        }

        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.INCR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
            log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
            return null;
        });
        log.info("点赞事件发送成功: {}", thumbEvent);
        return true;
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
        Long loginUserId = loginUser.getId();
        Long blogId = doThumbRequest.getBlogId();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本，点赞记录从 Redis 删除
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT_MQ,
                Arrays.asList(userThumbKey),
                blogId
        );
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new RuntimeException("用户未点赞");
        }
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
            redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), true);
            log.error("点赞事件发送失败: userId={}, blogId={}", loginUserId, blogId, ex);
            return null;
        });
        log.info("取消点赞事件发送成功: {}", thumbEvent);
        return true;
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
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

}
