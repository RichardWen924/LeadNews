package com.heima.user.controller.v1;

import com.heima.apis.user.IUserClient;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApUserRealnameService;
import com.heima.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserVerifyController implements IUserClient {


    @Autowired
    private ApUserRealnameService apUserRealnameService;

    @Autowired
    private ApUserService apUserService;

    /**
     * 查询列表
     */
    @PostMapping("/list")
    @Override
    public ResponseResult list(@RequestBody AuthDto dto) {
        return apUserRealnameService.findlist(dto);
    }

    /**
     * 审核失败
     */
    @PostMapping("/authFail")
    @Override
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return apUserRealnameService.authFail(dto);
    }

    /**
     * 审核通过
     */
    @PostMapping("/authPass")
    @Override
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return apUserRealnameService.authPass(dto);
    }

    @GetMapping("/api/v1/user/{id}")
    @Override
    public ResponseResult findUserById(@PathVariable("id") Integer id) {
        return ResponseResult.okResult(apUserService.getById(id));
    }
}
