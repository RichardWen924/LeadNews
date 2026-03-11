package com.heima.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询文章
     * @param dto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto dto);


    /**
     *  发布文章或保存草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    ResponseResult<WmNews> detail(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult delNews(Integer id);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);


    /**文章人工审核
     *
     * @param dto
     * @return
     */
    ResponseResult listVo(NewsAuthDto dto);

    ResponseResult findWmNewsById(Integer id);

    ResponseResult authFailNews(NewsAuthDto dto);

    ResponseResult authPassNews(NewsAuthDto dto);
}