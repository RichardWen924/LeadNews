package com.heima.apis.wemedia;

import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-wemedia")
public interface IWemediaClient {

    @GetMapping("/api/v1/channel/list")
    public ResponseResult getChannels();

    @PostMapping("/api/v1/news/list_vo")
    public ResponseResult listVo(@RequestBody NewsAuthDto dto);

    @GetMapping("/api/v1/news/one_vo/{id}")
    public ResponseResult findWmNewsById(@PathVariable("id") Integer id);

    @PostMapping("/api/v1/news/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto dto);

    @PostMapping("/api/v1/news/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDto dto);
}