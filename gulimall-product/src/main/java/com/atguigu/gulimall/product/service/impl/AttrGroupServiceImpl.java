package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //select * from pms_attr_group where catelog_id=?
        // and (attr_group_id=key or attr_group_name like %key%)
//        String key = (String) params.get("key");
//        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(catelogId != 0, "catelog_id", catelogId)
//                .and(obj -> {
//                    obj.eq(key != null, "attr_group_id", key)
//                            .or().like(key != null, "attr_group_name", key);
//                });
//
//        IPage<AttrGroupEntity> page = this.page(
//                new Query<AttrGroupEntity>().getPage(params),
//                queryWrapper);
//        return new PageUtils(page);
        //===============================================================================
        //这说明没有点击任何的树节点，直接查询全部
//        if (catelogId == 0) {
//            IPage<AttrGroupEntity> page = this.page(
//                    new Query<AttrGroupEntity>().getPage(params),
//                    new QueryWrapper<AttrGroupEntity>());
//            return new PageUtils(page);
//        } else {
//            String key = (String) params.get("key");

//            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("catelog_id", catelogId);
//            if (!StringUtils.isEmpty(key)) {
//                queryWrapper.and((obj) -> {
//                    obj.eq("attr_group_id", key)
//                            .or()
//                            .like("attr_group_name", key);
//                });
//            }
//            IPage<AttrGroupEntity> page = this.page(
//                    new Query<AttrGroupEntity>().getPage(params),
//                    queryWrapper);
//            return new PageUtils(page);
//        }
        //==========================================================================

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                queryWrapper.eq("attr_group_id", key).
                        or().
                        like("attr_group_name", key);
            });
        }

        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper);
            return new PageUtils(page);
        } else {
            queryWrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     *
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttr(Long catelogId) {
        //用于存储数据

        //先根据id把最基本的表group查出来
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().
                eq("catelog_id", catelogId));
        //这个查出来的数据我们可以直接拷贝到AttrGroupWithAttrsVo中
        List<AttrGroupWithAttrsVo> AttrGroupWithAttrsVos = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> relationAttr =
                    attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());


//        BeanUtils.copyProperties(attrGroupEntities, attrGroupWithAttrsVo);
        //因为他是一个集合，我们要将id生成一个新的list
        //准备数据


        //然后通attrGroupId这个查出对应的中间表的数据，然后取出中间表的id再去查找pms_attr，中的数据

        //这里如果没有数据直接丢出去
//        if (collect == null && collect.size() == 0) {
//            return AttrGroupWithAttrsVos;
//        }
//        Collection<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities =
//                relationService.listByIds(collect);
//        //准备数据
//        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(item -> {
//            Long attrId = item.getAttrId();
//            return attrId;
//        }).collect(Collectors.toList());
//        //查询数据一会可以给它里面的     private List<AttrEntity> attrs; 这个属性赋值
//        Collection<AttrEntity> attrEntities = attrService.listByIds(attrIds);
//

        return AttrGroupWithAttrsVos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithBySpuId(Long spuId, Long catalogId) {
        //1.查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        AttrGroupDao baseMapper = this.getBaseMapper();
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithBySpuId(spuId, catalogId);
        return vos;
    }
}