package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author:luosheng
 * @Date:2023-05-20 15:44
 * @Description:
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService feignService;

    //到es中进行检索
    @Override
    public SearchResult search(SearchParam param) {
        //1.动态构建出查询需要的DSL语句
        SearchResult result = null;

        //准备检索请求
        //构造检索buildSearchRequrest
        SearchRequest SearchRequest = buildSearchRequrest(param);


        try {
            //2.执行检索请求
            SearchResponse response = client.search(SearchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3.分析响应数据封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求
     * #模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存)，排序，分页，高亮，集合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequrest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构建DSL语句

        /**
         * 模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存)
         */
        //1.构建 bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1、must-模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            //这就就是模糊匹配（差不多就是搜索框的数据）
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2、bool - filter
        if (param.getCatalog3Id() != null) {
            //过滤中的第一个条件， 按照三级分类查询三级分类
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2 、bool -filter  按照品牌的id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            //过滤中条件 ，按照品牌id
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //1.2、bool - filter  -按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            //attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                //首先把id分割出来
                String[] s = attrStr.split("_");
                String attrId = s[0];//检索的属性id
                //把每个值分割出来

                String[] attrValue = s[1].split(":");//检索的属性的值
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                //每一个必须都得生成一个嵌入式的nested查询
                // ScoreMode.None：不参与评分
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQueryBuilder);
            }
        }

        //1.2 bool - filter - 按照库存是否有进行查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
            sourceBuilder.query(boolQuery);
        }
        //1.2 bool - filter  - 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            boolQuery.filter(rangeQuery);
            //1_500/_500
            //这个略微复杂，需要先按照_进行拆分，然后按照长度来看它输入的是什么
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //这说明它两个都输入了
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                //先判断是第一个还是第二个
                if (param.getSkuPrice().startsWith("_")) {//startsWith开始的
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {//endsWith结尾的
                    rangeQuery.gte(s[0]);
                }
            }
        }
        //把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);
        /**
         * 排序，分页，高亮
         */

        //2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //2.2、分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        // 2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            sourceBuilder.highlighter(builder);
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");

            sourceBuilder.highlighter(builder);
        }


        /**
         * 聚合分析
         */
        /**
         * 聚合分析
         */
        //1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);


        //1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg")
                .field("brandName").size(1));
        //1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg")
                .field("brandImg").size(1));

        sourceBuilder.aggregation(brand_agg);

        //2. 按照分类信息进行聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);

        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        sourceBuilder.aggregation(catalog_agg);

        //2. 按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //2.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        //2.1.1 在每个属性ID下，按照属性名进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //2.1.1 在每个属性ID下，按照属性值进行聚合
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        sourceBuilder.aggregation(attr_agg);

        log.info("构建的DSL语句 {}", sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * 构建查询的结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();

        //1.返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);

                }
                skuEsModels.add(esModel);
            }
        }
        result.setProducts(skuEsModels);


        //2.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");


        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1.得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            //2.得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).
                    getBuckets().get(0).getKeyAsString();
            //3. 得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).
                    getBuckets().stream().map(item -> {
                        String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                        return keyAsString;
                    }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);


            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        //3.当前所有商品涉及到的所有品牌信息
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        System.out.println(response.getAggregations());
        System.out.println(response);
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //1.得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            //得到品牌的名字
//            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).
//                    getBuckets().get(0).getKeyAsString();
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            //3.得到品牌的图片
            String brandImg =
                    ((ParsedStringTerms) bucket.getAggregations().
                            get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }

        result.setBrands(brandVos);
        //4.当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        ArrayList<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类的id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //=====以上信息都是从聚合信息中查询=====
        //5.分页信息-页码
        result.setPageNum(param.getPageNum());

        //5.分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5.分页信息-总页码  需要计算
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : (int) (total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            //6.构建面包屑导航功能
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attrs=2_5存:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);

                R r = feignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    String attrName = data.getAttrName();
                    navVo.setNavName(attrName);
                } else {
                    navVo.setNavName(s[0]);
                }

                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());


            result.setNavs(collect);
        }


        //品牌，分类添加面包屑
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");
            //TODO 远程查询所有品牌
            R brandinfos = feignService.infos(param.getBrandId());
            if (brandinfos.getCode() == 0) {
                List<BrandVo> brand = brandinfos.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");//浏览器对空格编码和java不一样所以要差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //2.取消了这个面包屑以后，我们要跳转到那个地方.将请求地址的url里面的内容置空
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }


}
