package com.heima.wemedia.service;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;

public interface WmSensitiveService {
    public ResponseResult list(SensitiveDto dto);

    ResponseResult save(AdSensitive dto);

    ResponseResult update(AdSensitive dto);

    ResponseResult delete(Integer id);
}
