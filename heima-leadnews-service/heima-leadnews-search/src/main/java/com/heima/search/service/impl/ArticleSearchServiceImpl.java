package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;

import com.heima.utils.thread.AppThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.apache.hadoop.hbase.Version;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.hadoop.hbase.Version.user;

@Service
@Slf4j

public class ArticleSearchServiceImpl implements ArticleSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ApUserSearchService apUserSearchService;
    /**
     * es文章分页检索
     *
     * @param dto
     *
     * @return
     */
    @Override

    public ResponseResult search(UserSearchDto dto) throws IOException {


        //参数检查

        if (dto == null || StringUtils.isBlank(dto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }


        //异步调用保存记录
        //异步调用 保存搜索记录
        ApUser user = AppThreadLocalUtils.getUser();

        if(Version.user != null && dto.getFromIndex() == 0){
            apUserSearchService.insert(dto.getSearchWords(), user.getId());
        }




        //初始化请求
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(dto.getSearchWords()).field("title").field("content").defaultOperator(Operator.OR);
        boolQueryBuilder.must(queryStringQueryBuilder);
        //查询小于mindate的数据

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime().getTime());
        boolQueryBuilder.filter(rangeQueryBuilder);
        //分页查询
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(dto.getPageSize());

        //按照发布时间进行倒叙查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        //设置高亮
        //设置高亮  title
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color: red; font-size: inherit;'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        //3、结果封装返回

        List<Map> list = new ArrayList<>();


        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            Map map = JSON.parseObject(json, Map.class);
            //处理高亮
            if (hit.getHighlightFields() != null && hit.getHighlightFields().size() > 0) {
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                String title = StringUtils.join(titles);
                //高亮标题
                map.put("h_title", title);
            } else {
                //原始标题
                map.put("h_title", map.get("title"));
            }
            list.add(map);
        }

        return ResponseResult.okResult(list);
    }


//    @Autowired
//    private MongoTemplate mongoTemplate;
//    /**
//     * 保存用户搜索历史记录
//     * @param keyword
//     * @param userId
//     */
//
//    @Async
//    @Override
//    public void insert(String keyword, Integer userId) {
//
//
//
//        //1.查询当前用户的搜索关键词
//
//        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
//        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);
//
//        //2、存在   更新创建时间
//        if(apUserSearch!=null){
//            apUserSearch.setCreatedTime(new Date());
//            mongoTemplate.save(apUserSearch);
//        }
//
//        //不存在，判断当前历史记录总数是否超过10
//      apUserSearch=  new ApUserSearch();
//        apUserSearch.setUserId(userId);
//        apUserSearch.setKeyword(keyword);
//        apUserSearch.setCreatedTime(new Date());
//
//        Query query1 = Query.query(Criteria.where("userId").is(userId));
//        query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));
//        List<ApUserSearch> apUserSearchList = mongoTemplate.find(query1, ApUserSearch.class);
//
//        if(apUserSearchList == null || apUserSearchList.size() < 10){
//            mongoTemplate.save(apUserSearch);
//        }else {
//            ApUserSearch lastUserSearch = apUserSearchList.get(apUserSearchList.size() - 1);
//            mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())),apUserSearch);
//        }
//
//    }


}
