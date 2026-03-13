package com.heima.admin.controller.v1;

import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private IWemediaClient wemediaClient;

    @PostMapping("/list_vo")
    public ResponseResult listVo(@RequestBody NewsAuthDto dto) {
        return wemediaClient.listVo(dto);
    }

    @GetMapping("/one_vo/{id}")
    public ResponseResult findWmNewsById(@PathVariable("id") Integer id) {
        return wemediaClient.findWmNewsById(id);
    }

    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto dto) {
        return wemediaClient.authFail(dto);
    }

    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDto dto) {
        return wemediaClient.authPass(dto);
    }
}
