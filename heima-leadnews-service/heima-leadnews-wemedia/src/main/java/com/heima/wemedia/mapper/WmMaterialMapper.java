package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmMaterialMapper extends BaseMapper<WmMaterial> {

    /**
     * 根据用户ID查询素材列表
     * 
     * @param userId 用户ID
     * @return 素材列表
     */
    List<WmMaterial> findByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID和收藏状态查询素材列表
     * 
     * @param userId       用户ID
     * @param isCollection 是否收藏 (0-未收藏, 1-已收藏)
     * @return 素材列表
     */
    List<WmMaterial> findByUserIdAndCollection(@Param("userId") Integer userId,
            @Param("isCollection") Short isCollection);

    /**
     * 根据ID列表批量查询素材
     * 
     * @param ids ID列表
     * @return 素材列表
     */
    List<WmMaterial> findByIds(@Param("ids") List<Integer> ids);

    /**
     * 统计用户素材数量
     * 
     * @param userId 用户ID
     * @return 素材数量
     */
    int countByUserId(@Param("userId") Integer userId);
}