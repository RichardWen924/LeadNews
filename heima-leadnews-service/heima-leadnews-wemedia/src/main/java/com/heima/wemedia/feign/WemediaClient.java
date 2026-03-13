package com.heima.wemedia.feign;

import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WemediaClient implements IWemediaClient {

    @Autowired
    private WmChannelService wmChannelService;

    @Autowired
    private WmNewsService wmNewsService;

    @GetMapping("/api/v1/channel/list")
    @Override
    public ResponseResult getChannels() {
        return wmChannelService.findAll();
    }

    @PostMapping("/api/v1/news/list_vo")
    @Override
    public ResponseResult listVo(@RequestBody NewsAuthDto dto) {
        return wmNewsService.listVo(dto);
    }

    @GetMapping("/api/v1/news/one_vo/{id}")
    @Override
    public ResponseResult findWmNewsById(@PathVariable("id") Integer id) {
        return wmNewsService.findWmNewsById(id);
    }

    @PostMapping("/api/v1/news/auth_fail")
    @Override
    public ResponseResult authFail(@RequestBody NewsAuthDto dto) {
        return wmNewsService.authFailNews(dto);
    }

    @PostMapping("/api/v1/news/auth_pass")
    @Override
    public ResponseResult authPass(@RequestBody NewsAuthDto dto) {
        return wmNewsService.authPassNews(dto);
    }
}