# gulimall

#### 介绍
购物商城

#### 软件架构
本项目以分布式为基础，使用的springBoot快速开发，其中，mysql做持久化，redis做缓存，其中使用到微服务等一系列的组件，如nacos作为注册中心，以及配置中心，feign做远程调用。


#### 安装教程
--自己想的配置环境流程没有试过，如果出现问题见招拆招 </br>
1.拉取代码后，需要配置环境，因为是前后端分离的一个项目，其中如果想启动后端代码，需要使用renren-fast-vue那个目录到我们如vsc工具中部署，具体流程可以看人人开发网址里面会用到node.js，等文件。</br> 2.然后，我们还需要配置，nacos地址 在本机部署 ip默认,然后项目文件中有sentinel,但到最后我使用了mq来解决了分布式的回滚，你也可以配置一个1.63版本的sentinel在本地，然后还需要在本机配置一个setat,代码中没有优化这块的内容，有很多地方可以做一些熔断降级限流等操作，启动ip为8719；</br>
3.然后我在linux中配置mysql，es，redis，nginx，rabbitmq，地址全是默认的，部署了在docker上 。linux的ip192.168.111.100.
#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
