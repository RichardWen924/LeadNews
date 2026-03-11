package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    /**
     * 敏感词新增
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdSensitive dto){
        return wmSensitiveService.save(dto);
    }

    /**
     * 敏感词修改
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdSensitive dto){
        return wmSensitiveService.update(dto);
    }


    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") Integer id){
        return wmSensitiveService.delete(id);
    }


}
