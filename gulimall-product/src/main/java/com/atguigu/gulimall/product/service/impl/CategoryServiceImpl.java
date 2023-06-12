package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.mysql.cj.log.Log;
import org.omg.CORBA.INTERNAL;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //private Map<String, Object> cache = new HashMap<>();
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;



    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    //这块自己写的,下面有个标准点的,都可运行
/*    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        List<CategoryEntity> collect = entities.stream().filter(value -> {
            return value.getParentCid() == 0;//找出所有的一级分类
        }).map((oneValue) -> {
            //拿到每个一级的分类，写一个方法，找二级，然后再递归找三级
            oneValue.setChildren(chilrens(oneValue, entities));
            return oneValue;
        }).sorted((n1, n2) -> {
            return (n1.getSort()==null?0:n1.getSort()) - (n2.getSort()==null?0:n2.getSort());
        }).collect(Collectors.toList());//拿到了一级分类以后我们，要查询它每给个属性的子属性

        return collect;
    }

    private List<CategoryEntity> chilrens(CategoryEntity oneValue, List<CategoryEntity> entities) {

        //先根据所有的属性，然后查询传来的属性的id做比较，所有属性拿parentCid
        List<CategoryEntity> collect = entities.stream().filter(v -> {
            return v.getParentCid() == oneValue.getCatId();
        }).map(shangmian -> {
            shangmian.setChildren(chilrens(shangmian, entities));
            return shangmian;
        }).sorted((n1, n2) -> {
            return (n1.getSort()==null?0:n1.getSort()) - (n2.getSort()==null?0:n2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }*/



    /*        *
     * 三级分类查询
     * 说白了，就是根据当前id查自己表中getParentCid和自己相同的，如果一样，则为我自己的子属性
     * 这样写其实我觉得不止可以三层，四级五级分类都可以直接查出来，很强
     * @return*/

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装称父子的树形结构

        //2.1) 找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(entitie -> {
            //一级分类的parentId=0,根据这个条件构建出一级分类的数据
            //这一块就是我们把一级分类的id给先过滤出来，(我看数据库就二十条)
            return entitie.getParentCid() == 0;

        }).map((menu) -> {
            //然后到这里我们就只有二十条的每一个数据，然后，我们把每一条的它的二级分类给找出来
            //就是找到每个菜单的子菜单，再把找出来的属性，给我们Children这个属性当中(这给属性是我们后来添加的，就是用来保存子菜单的),
            //这个menu就是一个一个的一级菜单，是否为一级二级菜单，是根据上面ParentCid判断出来的
            //这个entities就是总的所有的菜单，
            menu.setChildren(getChildrens(menu, entities));//这就相当于在entities中找到子菜单menu,这就类似个遍历
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList();
        //这里需要一个递归的查找，然后我们就写个方法，每次进去的适合，都把当前的节点先传给list，然后如果没有了就返回
        List<Long> parenPath = findParenPath(catelogId, list);//一个是当前节点的id，一个是list容器来存储

        //反转
        Collections.reverse(parenPath);
        return parenPath.toArray(new Long[parenPath.size()]);
    }

    private List<Long> findParenPath(Long catelogId, List<Long> list) {
        //进来第一件事，先把自己存进去，然后再看是否有父节点，如果有，就继续调用自己的方法。知道父节点为空把所有list返回回去
        list.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParenPath(byId.getParentCid(), list);
        }
        return list;
    }

    /*
     *
     *
     * @param root 当前菜单
     * @param all  所有菜单
     *             这个方法就是要找到所有的子菜单
     * @return
     */

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            //这个root就是当前每一个传进来的单个对象的属性，然后我们拿它的catid(也就相当于id)和所有的就每个属性的ParentCid做比较，如果相同则就属于 当前单个对象属性的子id
            //上句话所有单个对象属性就是每一次的遍历
            //如果是第二次就是给二级中childrens属性添加三级分类属性，就是当前子分类的（1级）中每一个二级 的三级属性
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1.找到子菜单，使用的是递归
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> { //这就一个排序，用当前顺序减去以前的顺序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

    /**
     * 级联更新所有有关的数据
     * 使用失效模式
     * 1.同时进行多种缓存操作  @Caching
     * 2.指定删除某个分区下的所有数据  @CacheEvict(value="category",allEntries = true)//一下子删除一个分区
     * 3.存储同一类型的数据，都可以指定成同一个分区.分区名默认就是缓存的前缀
     * @param category
     */

//    @Caching(evict = {
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key = "'getCatalogJson'")
//    })
    //第二种更新数据时删除缓存操作
    @CacheEvict(value="category",allEntries = true)//一下子删除一个分区
//   @CachePut  双写模式
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        //先进行当前数据的更新
        this.updateById(category);
        //更新冗余字段信息
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }


    /**
     * 1.每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 2.@Cachable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     *  3.默认行为
     *     1)、如果缓存中有，方法不用调用。
     *     2)、key默认自动生成，缓存的名字:会自动生成隔key值
     *     3)、缓存的value的值，默认使用jdk序列化机制，将序列化后的数据存到redis
     *     4)、默认ttl时间-1;永不过期
     *
     *   自定义:
     *      1)、指定生成的缓存使用的key    key属性指定，接受一给SpEL
     *         SpEL的详细可以查询官方文档
     *      2)、指定缓存的数据的存活时间   配置文件中修改ttl存活时间
     *      3)、将数据保存未json格式
     *
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name") //代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("有了缓存getLevel1Categorys只会调用一次");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return categoryEntities;
    }

    //TODO 产生堆外内存溢出：OutOfDirectMemorError
    //1.Springboot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信
    //2.lettuce的bug导致netty推外内存溢出 -Xmx300m; netty如果没有指定堆外内存。默认使用 -Xmx300m
    //解决方案，不能使用-Dio.netty.maxDirectMemory只去调大推外内存。
    //1.升级lettuce客户端，    2。切换使用jedis
    //redisTemplate:
    // lettuce、jedis操作的都是我们redis最底层的客户端,spring对他们进行了再次封装redisTemple
    //


    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库.............................................");
        //将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo category3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //给缓存中放json字符串，拿出的json，还用逆转为能用的对象类型；这就是【序列化与反序列化】

        /**
         * 1.空结果缓存:解决缓存穿透
         * 2.设置过期时间(加随机值):解决缓存雪崩
         * 3.加锁:解决缓存穿击
         */
        //1.加入缓存逻辑，缓存中存的数据是json字符串
        //JSONhao
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //2.缓存中如果没有。就调用查询数据库
            System.out.println("缓存未命中.....查询数据库");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            //然后第一次查直接返回
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中.......直接返回......");
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
        return result;
    }

    //占分布式锁 这个方法和下面的功能一样它只是个分布式锁的写法
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //这个就相当于一个坑位，不管多少人来，只有一人能占到
        String uuid = UUID.randomUUID().toString();
        String lockString = "lock";
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockString, uuid, 300, TimeUnit.SECONDS);
//当且仅当 key 不存在，将 key 的值设为 value ，并返回1；若给定的 key 已经存在，则 SETNX 不做任何动作，并返回0
        if (lock) {//如果为true则就是占到了
            //加锁成功...执行业务
            //但是这样只有一个地方释放锁，如果服务器这个时候宕机或出现问题，跳过了我们的删除锁
            //就会出现死锁，
            //解决死锁，，设置一个过期的时间，即使没有删除也自动删除（这一块可以直接写到加锁的时候，因为这里也可能宕机）
//            redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            System.out.println("获取分布式锁成功...");
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                log.error(".........................{}" + uuid);
                //获取值对比+对比成功删除=原子操作
                //String lock1 = redisTemplate.opsForValue().get("lock");
//                if (lock1.equals(uuid)) {
                //删除锁
                String script =
                        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 设置一下返回值类型 为Long
                // 因为删除判断的时候，返回的0,给其封装为数据类型。如果不封装那么默认返回String 类型，
                // 那么返回字符串与0 会有发生错误。
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class)
                        , Arrays.asList(lockString, uuid), lock);
                // redisTemplate.delete(lock2.toString());//相当于解锁操作
                // }
            }


            return dataFromDb;
        } else {
            //加锁失败...重试
            System.out.println("获取分布式锁失败...等待重试");
            try {
                //休眠100ms重试
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }


    }


    /**
     * 解决分布式中新增修改的一致性问题
     * 双写模式，就是每次更新数据，都把同一份数据在redis中更新一份
     * 失效模式，就是每次更新数据，都把数据库中的删除掉，等什么时候用了它自己再查询
     * @return
     */
    //使用redisson直接使用分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        String lockString = "CatalogJson-lock";
        RLock lock = redissonClient.getLock(lockString);
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;


    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {

            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                    new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });
            return result;
        }
        System.out.println("查询了数据库.............................................");

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {

            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getParentCid());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null,
                            l2.getCatId().toString(), l2.getName());

                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getParentCid());
                    if (level3Catelog != null) {

                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        String jsonString = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
        return parent_cid;
    }


    //从数据库查询并封装数据
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //1.如果缓存中有就用缓存的
//        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
//        if (cache.get("catalogJson") == null) {
//           //如果没有，调用业务
//            //返回数据放入缓存中
//        }
//       return catalogJson;

        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1.synchronized(this):在SpringBoot所有的组件在容器中都是单例的
        //TODO 本地锁，synchronized，JUC（Lock）它只能锁住我们当前进程的资源，而我们分布式情况下可能会有十来个这样的进程，必须使用分布式锁

        synchronized (this) {
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                //缓存不为null直接返回
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON,
                        new TypeReference<Map<String, List<Catelog2Vo>>>() {
                        });
                return result;
            }
            System.out.println("查询了数据库.............................................");
            /**
             * 我们下面写的代码需要很多次查询数据库，所以做出优化，将多次查询变为一次
             *
             */
            List<CategoryEntity> selectList = baseMapper.selectList(null);

            //查出所有分类

            //先查出一级分类，然后就可以拿着一级分类的id往下找
            List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
            //  List<CategoryEntity> level1Categorys = this.getLevel1Categorys();//其他方法写好的直接拿着用
            //封装数据
            Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                //每一个的一级分类,查到这个一级分类的二级分类
                List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getParentCid());
                List<Catelog2Vo> catelog2Vos = null;
                if (categoryEntities != null) {
                    catelog2Vos = categoryEntities.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null,
                                l2.getCatId().toString(), l2.getName());
                        // 找当前二级分类的三级分类封装成vo
                        List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getParentCid());
                        if (level3Catelog != null) {
                            //封装成level3的指定格式
                            List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                                Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catelog3Vo;
                            }).collect(Collectors.toList());
                            //在这里给上面那个null的数据
                            catelog2Vo.setCatalog3List(collect);
                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
            //3.查到的数据再放入缓存，将对象转为json放在缓存中
            String jsonString = JSON.toJSONString(parent_cid);
            redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
            return parent_cid;
        }
    }

    //

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }
}
