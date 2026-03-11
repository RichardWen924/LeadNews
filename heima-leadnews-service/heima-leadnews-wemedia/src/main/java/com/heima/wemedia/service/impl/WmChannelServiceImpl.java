package com.heima.wemedia.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {


    /**
     * 查询所有频道
     *
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }


    //list进行所有频道的展示


    /**
     * 分页查询频道列表
     */
    @Override
    public ResponseResult findListWithPage(ChannelDto dto) {
        // 1.参数检查
        if (dto == null) {
            return ResponseResult.errorResult(400, "参数错误");
        }
        // 2 分页查询
        IPage pageCheck = new Page(dto.getPage(), dto.getSize());
        // 3 按照不同需求查询
        LambdaQueryWrapper<WmChannel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //3.1 关键字模糊匹配
        if (StringUtils.isNotBlank(dto.getName())) {
            lambdaQueryWrapper.like(WmChannel::getName, dto.getName());
        }
        //3.2 排序
        lambdaQueryWrapper.orderByDesc(WmChannel::getCreatedTime);
        pageCheck = page(pageCheck, lambdaQueryWrapper);
        //4. 返回结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageCheck.getTotal());
        responseResult.setData(pageCheck.getRecords());
        return responseResult;
    }


    /**
     * 更新频道
     *
     * @param channel
     * @return
     */
    @Override
    public ResponseResult updateChannel(AdChannel channel) {
        // 1.参数检查
        if (channel == null) {
            return ResponseResult.errorResult(400, "参数错误");
        }
        // 2.更新
        WmChannel wmChannel = new WmChannel();
        BeanUtils.copyProperties(channel, wmChannel);
        updateById(wmChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    @Override
    public ResponseResult deleteChannel(Integer id) {
        // 1.参数检查
        if (id == null) {
            return ResponseResult.errorResult(400, "参数错误");
        }
        // 2.删除
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }
}

