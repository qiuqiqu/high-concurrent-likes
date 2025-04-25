package com.gy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gy.model.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author yun.guo
 * @description 针对表【blog】的数据库操作Mapper
 * @createDate 2025-04-22 17:52:31
 * @Entity com.gy.model.Blog
 */
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




