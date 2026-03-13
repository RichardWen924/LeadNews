package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.user.IUserClient;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.comment.service.CommentService;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IUserClient userClient;

    @Autowired
    private GreenTextScan greenTextScan;

    @Override
    public ResponseResult saveComment(CommentSaveDto dto) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getContent()) || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getContent().length() > 140) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能超过140字");
        }

        //2.判断是否登录
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3.敏感词过滤
        try {
            Map map = greenTextScan.greeTextScan(dto.getContent());
            if (map != null && !map.get("suggestion").equals("pass")) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容包含非法关键词");
            }
        } catch (Exception e) {
            log.error("敏感词过滤失败", e);
        }

        //4.保存评论
        ApUser dbUser = null;
        ResponseResult userResult = userClient.findUserById(user.getId());
        if (userResult.getCode().equals(0) && userResult.getData() != null) {
            dbUser = JSON.parseObject(JSON.toJSONString(userResult.getData()), ApUser.class);
        }

        ApComment apComment = new ApComment();
        apComment.setAuthorId(user.getId());
        apComment.setAuthorName(dbUser != null ? dbUser.getName() : "匿名用户");
        apComment.setContent(dto.getContent());
        apComment.setCreatedTime(new Date());
        apComment.setEntryId(dto.getArticleId());
        apComment.setLikes(0);
        apComment.setReply(0);
        apComment.setType(0);
        apComment.setFlag(0);

        mongoTemplate.save(apComment);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult like(CommentLikeDto dto) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getCommentId()) || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.判断是否登录
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3.查询评论
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class, "ap_comment");
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //4.点赞或取消点赞
        if (dto.getOperation() == 0) {
            //点赞
            ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").is(dto.getCommentId())), ApCommentLike.class, "ap_comment_like");
            if (apCommentLike != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已点赞");
            }

            apCommentLike = new ApCommentLike();
            apCommentLike.setAuthorId(user.getId());
            apCommentLike.setCommentId(dto.getCommentId());
            mongoTemplate.save(apCommentLike, "ap_comment_like");

            apComment.setLikes(apComment.getLikes() + 1);
            mongoTemplate.save(apComment, "ap_comment");
        } else {
            //取消点赞
            ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").is(dto.getCommentId())), ApCommentLike.class, "ap_comment_like");
            if (apCommentLike == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "未点赞");
            }

            mongoTemplate.remove(apCommentLike, "ap_comment_like");

            apComment.setLikes(Math.max(0, apComment.getLikes() - 1));
            mongoTemplate.save(apComment, "ap_comment");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("likes", apComment.getLikes());
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult findByArticleId(CommentDto dto) {
        //1.检查参数
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        int size = dto.getSize() == null ? 10 : dto.getSize();

        //2.查询评论
        Query query = Query.query(Criteria.where("entryId").is(dto.getArticleId()));
        if (dto.getMinDate() != null) {
            query.addCriteria(Criteria.where("createdTime").lt(dto.getMinDate()));
        }
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApComment> list = mongoTemplate.find(query, ApComment.class, "ap_comment");

        //3.判断是否点赞
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.okResult(list);
        }

        List<String> commentIds = list.stream().map(ApComment::getId).collect(Collectors.toList());
        List<ApCommentLike> apCommentLikes = mongoTemplate.find(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").in(commentIds)), ApCommentLike.class, "ap_comment_like");

        List<Map> resultList = new ArrayList<>();
        for (ApComment apComment : list) {
            Map map = JSON.parseObject(JSON.toJSONString(apComment), Map.class);
            for (ApCommentLike apCommentLike : apCommentLikes) {
                if (apCommentLike.getCommentId().equals(apComment.getId())) {
                    map.put("operation", 0);
                    break;
                }
            }
            resultList.add(map);
        }

        return ResponseResult.okResult(resultList);
    }
}
