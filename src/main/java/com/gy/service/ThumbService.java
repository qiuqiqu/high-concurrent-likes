package com.gy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy.model.dto.thumb.DoThumbRequest;
import com.gy.model.entity.Thumb;
import jakarta.servlet.http.HttpServletRequest;

;

/**
 * @author yun.guo
 * @description 针对表【thumb】的数据库操作Service
 * @createDate 2025-04-22 17:53:00
 */
public interface ThumbService extends IService<Thumb> {
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean hasThumb(Long blogId, Long userId);

}
