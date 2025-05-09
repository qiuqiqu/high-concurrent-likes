package com.gy.controller;


import com.gy.common.BaseResponse;
import com.gy.common.ResultUtils;
import com.gy.model.dto.thumb.DoThumbRequest;
import com.gy.service.ThumbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author gy
 */
@Tag(name = "点赞")
@RestController
@RequestMapping("thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    @Operation(summary = "点赞")
    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.doThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        Boolean success = thumbService.undoThumb(doThumbRequest, request);
        return ResultUtils.success(success);
    }

}
