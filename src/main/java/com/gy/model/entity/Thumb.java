package com.gy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 点赞
 * @TableName thumb
 */
@TableName(value ="thumb")
@Data
public class Thumb {
    /**
     * 点赞id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 创建时间
     */
    private Date createTime;
}