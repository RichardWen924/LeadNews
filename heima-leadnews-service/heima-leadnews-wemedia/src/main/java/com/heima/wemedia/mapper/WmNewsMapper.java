package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmNews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmNewsMapper extends BaseMapper<WmNews> {

    /**
     * 根据用户ID查询文章列表
     * 
     * @param userId 用户ID
     * @return 文章列表
     */
    List<WmNews> findByUserId(@Param("userId") Integer userId);

    /**
     * 根据状态查询文章列表
     * 
     * @param status 文章状态
     * @return 文章列表
     */
    List<WmNews> findByStatus(@Param("status") Short status);

    /**
     * 根据用户ID和状态查询文章列表
     * 
     * @param userId 用户ID
     * @param status 文章状态
     * @return 文章列表
     */
    List<WmNews> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") Short status);

    /**
     * 根据文章ID和用户ID查询文章（确保用户只能查看自己的文章）
     * 
     * @param id     文章ID
     * @param userId 用户ID
     * @return 文章
     */
    WmNews findByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 统计用户文章数量
     * 
     * @param userId 用户ID
     * @return 文章数量
     */
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 统计指定状态的文章数量
     * 
     * @param status 文章状态
     * @return 文章数量
     */
    int countByStatus(@Param("status") Short status);
}