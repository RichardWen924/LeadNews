package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;


    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannel channel){

        WmChannel wmChannel = new WmChannel();
        BeanUtils.copyProperties(channel,wmChannel);
        if(wmChannel.getIsDefault()==null){
            wmChannel.setIsDefault(true);
        }
        if(wmChannel.getOrd()==null){
            wmChannel.setOrd(1);
        }
        if(wmChannel.getCreatedTime()==null){
            wmChannel.setCreatedTime(new Date());
        }
        return ResponseResult.okResult(wmChannelService.save(wmChannel));
    }

    @PostMapping("/list")
    public ResponseResult findListWithPage(@RequestBody ChannelDto dto){
        return wmChannelService.findListWithPage(dto);
    }

//更新频道
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel channel){
        return wmChannelService.updateChannel(channel);
    }

}