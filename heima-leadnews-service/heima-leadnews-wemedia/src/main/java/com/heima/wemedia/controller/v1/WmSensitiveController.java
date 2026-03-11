package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {
    @Autowired
    private WmSensitiveService wmSensitiveService;
    /**
     * 敏感词列表
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody SensitiveDto dto){
        return wmSensitiveService.list(dto);
    }

}
