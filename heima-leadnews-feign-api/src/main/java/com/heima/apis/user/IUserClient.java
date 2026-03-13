package com.heima.apis.user;

import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-user")
public interface IUserClient {

    @PostMapping("/api/v1/auth/list")
    public ResponseResult list(@RequestBody AuthDto dto);

    @PostMapping("/api/v1/auth/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto);

    @PostMapping("/api/v1/auth/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto);
}
