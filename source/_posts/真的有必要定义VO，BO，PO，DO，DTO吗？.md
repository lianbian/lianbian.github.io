---
title: 真的有必要定义VO，BO，PO，DO，DTO吗？
keywords: VO BO PO DO DTO
categories: java
summary: 大家在项目中有用到VO，BO，PO，DO，DTO吗？知道为什么需要定义这些看似相同的类吗？
author: 连边
date: 2022-04-20
tags:
  - java
---

大家好，我是连边。

今天给大家带来一篇关于`VO，BO，PO，DO，DTO`的文章，阅读完这篇文章之后，希望大家对`VO，BO，PO，DO，DTO`有自己的见解。

![VO，BO，PO，DO，DTO](https://mkstatic.lianbian.net/202204201300400.png)



## 概念

在讲具体的概念之前，我们先简单的讲一讲我们`MVC`开发模式。

**MVC的简单定义：**

`M`层负责与数据库打交道；

`C`层负责业务逻辑的编写；

`V`层负责给用户展示（针对于前后端不分离的项目，不分离项目那种编写模版的方式，理解`V`的概念更直观）。



而我们今天要说的`VO，BO，PO，DO，DTO`呢，就是穿梭在这`M、V、C`层之间的`实体传输对象`。

![实体传输对象示意图](https://mkstatic.lianbian.net/202204201306683.png)

- VO（`View Object`）：**视图对象**，用于展示层，它的作用是把某个指定页面（或组件）的所有数据封装起来。
- DTO（`Data Transfer Object`）：**数据传输对象**，这个概念来源于J2EE的设计模式，原来的目的是为了EJB的分布式应用提供粗粒度的数据实体，以减少分布式调用的次数，从而提高分布式调用的性能和降低网络负载，但在这里，更符合泛指用于展示层与服务层之间的数据传输对象。
- BO（`Business Object`）：**业务对象**，把业务逻辑封装为一个对象，这个对象可以包括一个或多个其它的对象。
- PO（`Persistent Object`）：**持久化对象**，它跟持久层（通常是关系型数据库）的数据结构形成一一对应的映射关系，如果持久层是关系型数据库，那么，数据表中的每个字段（或若干个）就对应PO的一个（或若干个）属性。
- DO（`Domain Object`）：**领域对象**，就是从现实世界中抽象出来的有形或无形的业务实体。



## 有必要用吗？

项目中真的有必要定义`VO，BO，PO，DO，DTO`吗？

还是要理性看待这个问题，要看我们项目“目的地”是什么。

如果项目比较小，是一个简单的`MVC`项目，又是`单兵作战`，我不建议使用`VO，BO，PO，DO，DTO`，直接用`POJO`负责各个层来传输就好，因为这种项目的“目的地”是快速完成。

而我们更多的时候，是持续迭代的团队协作项目，这个时候我们就建议用`VO，BO，PO，DO，DTO`，而且团队内要达成共识，形成一个`标准规范`。

1. 业务复杂，人员协同性要求高的场景下，这些规范性的东西不按着来虽然不会出错，程序照样跑，但是遵守规范会让程序更具扩展性和可读性；
2. 让类语义更明确，很容易知道类的含义；

其实就是提升项目的`可扩展性`、`可维护性`与`可阅读性`。

提升这些性能的尽头是`经济效益`。



## 总结

这篇文章很短，最后稍微总结一下，不管用哪种方式，只要团队内定义好一种适应的协同规范就行。

没有一个`绝对好`与`绝对坏`的方式方法。

团队规范的尽头能提升项目的`可扩展性`、`可维护性`与`可阅读性`，从而降低bug率。



另附这些概念命名规范：

- 数据对象：xxxPO，xxx即为数据表名。(也可DO)
- 数据传输对象：xxxDTO，xxx为业务领域相关的名称。
- 展示对象：xxxVO，xxx一般为网页名称。
- 业务对象：xxxBO，xxx是业务名称。



参考链接：https://mp.weixin.qq.com/s/3-wA3hh75pgoTJu0Rz3SXw



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202204201259459.jpg)

