package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentRepayDto;
import com.heima.model.comment.dtos.CommentRepayLikeDto;
import com.heima.model.comment.dtos.CommentRepaySaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface CommentRepayService {

    /**
     * 保存回复
     * @param dto
     * @return
     */
    ResponseResult saveCommentRepay(CommentRepaySaveDto dto);

    /**
     * 点赞回复
     * @param dto
     * @return
     */
    ResponseResult like(CommentRepayLikeDto dto);

    /**
     * 查询评论回复列表
     * @param dto
     * @return
     */
    ResponseResult loadCommentRepay(CommentRepayDto dto);
}
