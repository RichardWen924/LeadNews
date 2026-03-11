package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.AuthDto;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {
    @Override
    public ResponseResult findlist(AuthDto dto) {
        if(dto == null) {
            return ResponseResult.errorResult(400, "参数错误");
        }
        // 2 分页查询
        IPage pageCheck=new Page(dto.getPage(),dto.getSize());
        // 3 按照不同需求查询
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //3.1 状态
        if(dto.getStatus()!=null){
            lambdaQueryWrapper.eq(ApUserRealname::getStatus, dto.getStatus());
        }
        //3.2 排序
        lambdaQueryWrapper.orderByDesc(ApUserRealname::getCreatedTime);
        pageCheck = page(pageCheck, lambdaQueryWrapper);
        //4. 返回结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageCheck.getTotal());
        responseResult.setData(pageCheck.getRecords());
        return responseResult;
    }
}
