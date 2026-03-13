package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.user.IUserClient;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.pojos.ApCommentRepay;
import com.heima.comment.service.CommentRepayService;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.model.comment.dtos.CommentRepayDto;
import com.heima.model.comment.dtos.CommentRepayLikeDto;
import com.heima.model.comment.dtos.CommentRepaySaveDto;
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
public class CommentRepayServiceImpl implements CommentRepayService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IUserClient userClient;

    @Autowired
    private GreenTextScan greenTextScan;

    @Override
    public ResponseResult saveCommentRepay(CommentRepaySaveDto dto) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getContent()) || StringUtils.isBlank(dto.getCommentId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getContent().length() > 140) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "回复内容不能超过140字");
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
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "回复内容包含非法关键词");
            }
        } catch (Exception e) {
            log.error("敏感词过滤失败", e);
        }

        //4.保存回复
        ApUser dbUser = null; // Initialize dbUser
        // 2.检查用户状态
        ResponseResult userResult = userClient.findUserById(user.getId());
        if (userResult.getCode().equals(AppHttpCodeEnum.SUCCESS.getCode()) && userResult.getData() != null) {
            dbUser = JSON.parseObject(JSON.toJSONString(userResult.getData()), ApUser.class);
        }

        ApCommentRepay apCommentRepay = new ApCommentRepay();
        apCommentRepay.setAuthorId(user.getId());
        apCommentRepay.setAuthorName(dbUser != null ? dbUser.getName() : "匿名用户");
        apCommentRepay.setContent(dto.getContent());
        apCommentRepay.setCreatedTime(new Date());
        apCommentRepay.setCommentId(dto.getCommentId());
        apCommentRepay.setLikes(0);

        mongoTemplate.save(apCommentRepay, "ap_comment_repay");

        //5.更新评论的回复数量
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class, "ap_comment");
        if (apComment != null) {
            apComment.setReply(apComment.getReply() + 1);
            mongoTemplate.save(apComment, "ap_comment");
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult like(CommentRepayLikeDto dto) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getCommentRepayId()) || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.判断是否登录
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3.查询回复
        ApCommentRepay apCommentRepay = mongoTemplate.findById(dto.getCommentRepayId(), ApCommentRepay.class, "ap_comment_repay");
        if (apCommentRepay == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //4.点赞或取消点赞
        if (dto.getOperation() == 0) {
            //点赞
            ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").is(dto.getCommentRepayId())), ApCommentLike.class, "ap_comment_like");
            if (apCommentLike != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已点赞");
            }

            apCommentLike = new ApCommentLike();
            apCommentLike.setAuthorId(user.getId());
            apCommentLike.setCommentId(dto.getCommentRepayId());
            mongoTemplate.save(apCommentLike, "ap_comment_like");

            apCommentRepay.setLikes(apCommentRepay.getLikes() + 1);
            mongoTemplate.save(apCommentRepay, "ap_comment_repay");
        } else {
            //取消点赞
            ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").is(dto.getCommentRepayId())), ApCommentLike.class, "ap_comment_like");
            if (apCommentLike == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "未点赞");
            }

            mongoTemplate.remove(apCommentLike, "ap_comment_like");

            apCommentRepay.setLikes(Math.max(0, apCommentRepay.getLikes() - 1));
            mongoTemplate.save(apCommentRepay, "ap_comment_repay");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("likes", apCommentRepay.getLikes());
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult loadCommentRepay(CommentRepayDto dto) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getCommentId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        int size = dto.getSize() == null ? 10 : dto.getSize();

        //2.查询回复
        Query query = Query.query(Criteria.where("commentId").is(dto.getCommentId()));
        if (dto.getMinDate() != null) {
            query.addCriteria(Criteria.where("createdTime").lt(dto.getMinDate()));
        }
        query.limit(size);
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApCommentRepay> list = mongoTemplate.find(query, ApCommentRepay.class, "ap_comment_repay");

        //3.判断是否点赞
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.okResult(list);
        }

        List<String> commentRepayIds = list.stream().map(ApCommentRepay::getId).collect(Collectors.toList());
        List<ApCommentLike> apCommentLikes = mongoTemplate.find(Query.query(Criteria.where("authorId").is(user.getId()).and("commentId").in(commentRepayIds)), ApCommentLike.class, "ap_comment_like");

        List<Map> resultList = new ArrayList<>();
        for (ApCommentRepay apCommentRepay : list) {
            Map map = JSON.parseObject(JSON.toJSONString(apCommentRepay), Map.class);
            boolean liked = false;
            for (ApCommentLike apCommentLike : apCommentLikes) {
                if (apCommentLike.getCommentId().equals(apCommentRepay.getId())) {
                    map.put("operation", 0);
                    liked = true;
                    break;
                }
            }
            if (!liked) {
                map.put("operation", null);
            }
            resultList.add(map);
        }

        return ResponseResult.okResult(resultList);
    }
}
