package com.heima.user.controller.v1;

import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserVerifyController {


    @Autowired
    private ApUserRealnameService apUserRealnameService;
    /**
     * 查询列表
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody AuthDto dto) {
        return apUserRealnameService.findlist(dto);
    }

    /**
     * 审核失败
     */
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return apUserRealnameService.authFail(dto);
    }

    /**
     * 审核通过
     */
    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return apUserRealnameService.authPass(dto);
    }



}
