package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AuthDto;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    ResponseResult findlist(AuthDto dto);

    ResponseResult authFail(AuthDto dto);

    ResponseResult authPass(AuthDto dto);
}
