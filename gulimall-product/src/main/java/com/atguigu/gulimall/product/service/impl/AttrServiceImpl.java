package com.atguigu.gulimall.product.service.impl;

import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //将attr的所有属性复制给attrEntity
        BeanUtils.copyProperties(attr, attrEntity);
        //1.保存基本数据
        this.save(attrEntity);
        //保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnm.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {

        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().
                eq("attr_type", "base".equalsIgnoreCase(attrType) ?
                        ProductConstant.AttrEnm.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnm.ATTR_TYPE_SALE.getCode());//加一个类型判断，因为我们这个查询要供两个小模块使用
        //如果为base，那就是1，否则为0-销售属性;
        // 构造qw下面使用，
        String key = (String) params.get("key");//拿出模糊查询的key值
        if (catelogId != 0) {//如果我们点击了那个左侧的栏，就会发出一个根据catelogid的数值，它没有点击默认vue的data我们给的0
            queryWrapper.eq("catelog_id", catelogId);
        }
        if (!StringUtils.isEmpty(key)) { //如果模糊查的栏里有数据就会进入该查询
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).
                        or().
                        eq("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(   //标准分页查询
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        //给我们的那个分页中增添数据，先从getRecords中取出所有的实体类
        List<AttrEntity> records = page.getRecords();
        //然后来个遍历，拿出list中的每一个实体类数据，然后做把所有的attrEntity数据，先复制，后赋值，到我们vo那个返回前端的实体类当中。

        List<AttrRespVo> attr_id = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);//将attrEntit复制给attrRespVo

            if ("base".equalsIgnoreCase(attrType)) {
                //1.设置分类和分组的名字
                //先从这个分组的中间表中取出attrid
                AttrAttrgroupRelationEntity relationEntity = relationDao.
                        selectById(attrEntity.getAttrId());
                //非空判断
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.
                            selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        //查出来了数据再把att_grup_name的数据放到前端要的数据当中
                        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            //这个表中属性就有catelogid ，所以不用中间表，可直接查询另外的那张表
            CategoryEntity categoryEntity = categoryDao.
                    selectById(attrEntity.getCatelogId());
            //先做非空判断
            if (categoryEntity != null) {
                //然后如果有就把查出的数据的name赋给我们attrrespvo当中
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attr_id);
        return pageUtils;
    }

    @Cacheable(value = "attr",key = "'attrinfo:'+#root.args[0]")

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();//用于存储数据，然后返回的就是他
        AttrEntity attrEntity = this.getById(attrId);//先查出所有可以直接查出来的数据
        //将attrEntity拷贝到respVo当中
        BeanUtils.copyProperties(attrEntity, respVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnm.ATTR_TYPE_BASE.getCode()) {
            //先根据attrId查询它的中间表，然后可以从中间表中取出相对应的数据
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().
                    eq("attr_id", attrId));
            if (relationEntity != null) {
                respVo.setAttrGroupId(relationEntity.getAttrGroupId());//查出来先把分组id给他
                //然后根据分组id查询分组名字
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        //这个server我们已经写好了一个查询完整路径的方法，我们直接用就ok了
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);

        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }

        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        //先做一个基本的修改
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);


        if (attrEntity.getAttrType() == ProductConstant.AttrEnm.ATTR_TYPE_BASE.getCode()) {
            //我们要修改这个分组的实体类
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            //要先给我们要修改的字段加上
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            //有可能我们当前表就没有中间表，所以还要再写个新增，如果没有，就新增
            Integer integer = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().
                    eq("attr_id", attr.getAttrId()));
            if (integer > 0) {//说明有数据，直接执行修改
                //然后根据attr_id进行修改
                //这个修改的是中间表
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().
                        eq("attr_id", attr.getAttrId()));
            } else {//说明没有中间表，要执行新增加一个
                relationDao.insert(relationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attr_group_id = relationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().
                        eq("attr_group_id", attrgroupId));

        List<Long> attrIds = attr_group_id.stream().map((entity) -> {
            return entity.getAttrId();
        }).collect(Collectors.toList());
        Collection<AttrEntity> attrEntities = null;
        //这里如果不加可能会出现异常
        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }

        attrEntities = this.listByIds(attrIds);

        return (List<AttrEntity>) attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        //[{"attrId":1,"attrGroupId":2}]
        //1.我们自己编写一个sql，然后实现根据它俩个的id实现单个或批量的删除，要用到一个for

        //先将vos转换称list，然后在里面可以进行每个单属性的操作
        //然后将生成好的一个一个实体类返回到一个新的List当中
        List<AttrAttrgroupRelationEntity> entites = Arrays.asList(vos).stream().map((entity) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(entity, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        //有了这个实体类，就可以调用分组dao这个表，然后把实体类赛进去，最后拿出数据 然后到xml就mybatis里，进行一个遍历赋值删除，
        relationDao.deleteBatchRelation(entites);

    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1.当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();//拿出CatelogId(就是我们三级分类那个属性)
        //2.当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的别的分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().
                eq("catelog_id", catelogId)); //它的id必须是当前分类，就是catelogId这给类下
        //准备数据，我们需要的是已经排除好的每个分组id
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2)、这些分组关联的属性
        QueryWrapper<AttrAttrgroupRelationEntity> attrGroupIdQW = new QueryWrapper<AttrAttrgroupRelationEntity>().
                in("attr_group_id", collect);

        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(attrGroupIdQW);


        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());//这个生成的id下面查询要排除掉它

        //2.3)、从当前分类的所有属性中移除这些属性
        //并且它的id不能属于我们准备好的那个id
        //这个时候查出的所有数据就是我们可以添加关联的表
        //这个查询是最最主要的查询，其他知识准备一个条件罢了
        QueryWrapper<AttrEntity> qw = new QueryWrapper<AttrEntity>().
                eq("catelog_id", catelogId).//查出的这个attr，就属性那个表，必须catelog_id为当前的
                        eq("attr_type", ProductConstant.AttrEnm.ATTR_TYPE_BASE.getCode());//必须是基本属性才能查询
        if (attrIds != null && attrIds.size() > 0) {
            qw.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            qw.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                qw);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        /**
         * SELECT attr_id FROM pms_attr WHERE attr_id IN(?) AND search_type=1
         */
        return baseMapper.selectSearchAttrIds(attrIds);
    }

}