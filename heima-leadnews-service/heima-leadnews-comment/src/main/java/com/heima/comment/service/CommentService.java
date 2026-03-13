package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface CommentService {

    /**
     * 保存评论
     * @param dto
     * @return
     */
    ResponseResult saveComment(CommentSaveDto dto);

    /**
     * 点赞评论
     * @param dto
     * @return
     */
    ResponseResult like(CommentLikeDto dto);

    /**
     * 查询文章评论列表
     * @param dto
     * @return
     */
    ResponseResult findByArticleId(CommentDto dto);
}
