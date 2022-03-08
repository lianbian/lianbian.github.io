---
img: http://mkstatic.lianbian.net/202203082153007.png
title: ClickHouse介绍与特点
keywords: clickhouse
categories: clickhouse
summary: 基础的ClickHouse的介绍，讲一讲什么场景下我们使用ClickHouse。
author: 连边
date: 2022-03-08
tags:
  - clickhouse
  - 技术选型
---

大家好，我是连边。



今天给大家介绍`ClickHouse`，文章整体不长，但是让大家对它有一个整体的认识。

>  ClickHouse是由俄罗斯IT公司Yandex开发的一款用于联机分析处理（OLAP）的开源列式数据库。于2016年6月（Apache许可证）开源。



老规矩，还是先上导读图，然后开始～

![ClickHouse介绍与特点](http://mkstatic.lianbian.net/202203082153007.png)



## 业务（数据存储）与中间件的关系

我们业务对数据的操作，无外乎`增删改查`，然后根据不同的业务特点，使用了不同的中间件来实现业务。

比如搜索领域（查询），我们会用有[Elasticsearch](https://www.elastic.co/)、Solr；平常的工作中做一般的增删改查，会毫不犹豫的用`MySQL`；当要做缓存的时候，立马会想到`Redis`。

但业务总是不断变化的，当中间件满足不了具体的业务需求的时候，就会催生的新的中间件（产品）。

`ClickHouse`就是为了降低大数据存储的成本，为了降低技术使用门槛而催生的一款优秀的中间件（产品）。相比`Hadoop`体系，降低了运维成本；在使用语法层面，几乎和`MySQL`没有区别，都是使用`DDL`、`SQL`这一套体系。



## ClickHouse适合的场景是什么

1. 数据量比较大，如果一般的功能性的数据，不要用`ClickHouse`，比如做一个OA人事管理系统，这就是功能性的系统；
2. 静态数据，比如工业设备产生的追溯数据；
3. 不需要全文检索，需要全文检索一般的方案是在`ClickHouse`上层添加`ES`中间件；

之前准备用`ES`来实现我的业务，后来对`ClickHouse`有了一定的了解之后，发现`ClickHouse`更适用。

我的业务场景：搜集工业设备上报的实验数据，在必要的时候要进行数据追溯（无全文检索），有一些实时数据也需要及时展示。

`ES`是出了名的吃服务器的，如果`ClickHouse`满足你的业务，尽可能用`ClickHouse`；

网上有人做了对比，有兴趣的可以看一看。

[Elasticsearch和Clickhouse基本查询对比](https://zhuanlan.zhihu.com/p/353296392)



## 一些术语的解释

1. 宽表

   包含了很多个列的大表，“宽”字很形象；

2. OLAP&OLTP

   在线事务处理（Online Transaction Processing、OLTP）和在线分析处理（Online Analytical Processing、OLAP）是数据库最常见的两种场景，这两种场景不是唯二的两种，从中衍生出来的还有混合事务分析处理（Hybrid Transactional/Analytical Processing、HTAP）3等概念。

   在线事务处理（OLTP）是最常见的场景，在线服务需要为用户实时提供服务，提供服务的过程中可能要查询或者创建一些记录；

   在线分析处理（OLAP）的场景需要批量处理用户数据，数据分析师会根据用户产生的数据分析用户行为和画像、产出报表和模型。

3. 列式存储

推荐一篇我写的文章，很清晰的讲清楚了 [什么是列式存储](https://mp.weixin.qq.com/s/AHuzT_k2XhF5HVEACOl1-w)



## 学习资料的推荐

1. [ClickHouse官方](https://clickhouse.com/docs/en/) 

   推荐英文版，虽然有中文版，但是有很多代码示例，只有英文版本有。

2. [尚硅谷视频](https://www.bilibili.com/video/BV1Yh411z7os?p=1)

   整体`64`个视频，用1.5倍数，配上虚拟机边观看，边实战一遍，大概2天完整的时间能够搞定。

3. [连边ClickHouse](https://www.lianbian.net/categories/clickhouse/)

   我整理的关于ClickHouse一些文章，前期理念与实战，后期也会更新线上遇到的问题。



## 总结

今天这篇文章写到这里就结束了，在我们平常工作中，一定要注意仔细剖析业务，选择合适的中间件来实现业务，做到事半功倍的效果。

我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

可以关注`订阅号@连边`不错过精彩文章

![订阅号@连边](http://mkstatic.lianbian.net/202203082207503.jpg)