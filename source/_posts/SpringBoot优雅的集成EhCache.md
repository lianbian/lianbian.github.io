---
title: SpringBoot优雅的集成EhCache
keywords: java EhCache
categories: java
summary: 在SpringBoot下，优雅的使用EhCache
author: 连边
date: 2022-03-15
tags:
  - java
---

大家好，我是连边。

今天给大家带来`EhCache`在`SpringBoot`框架下使用实战。



## 简介

`EhCache` 是一个纯Java的`进程内`缓存框架，具有快速、精干等特点。

上边的概念注意关键词`进程内`，他其实传递了两个关键点：

1. 速度快（进程内剔除了各种切换资源的损耗）；
2. 不跨进程。

现在我们说到缓存，很多人会不暇思索的使用`Redis`，其实在Java生态下，有很多场景我们用`EhCache`更合理。

简单的区分一下什么时候使用`Redis`，什么时候使用`EhCache`呢？

当我们的缓存需要多个进程（分布式）共享的时候，使用`Redis`，如果只是单机、单应用内的常见缓存，使用`EhCache`。



## SpringBoot集成EhCache

### 创建项目

![创建Maven项目](https://mkstatic.lianbian.net/202203151227457.png)



### 引入依赖

分别引入spring-boot-starter-web、spring-boot-starter-cache、cache-api、ehcache四个包

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.6.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
            <version>2.6.1</version></dependency>
        <dependency>
            <groupId>javax.cache</groupId>
            <artifactId>cache-api</artifactId>
            <version>1.1.1</version>
        </dependency>
        <dependency>
            <groupId>org.ehcache</groupId>
            <artifactId>ehcache</artifactId>
            <version>3.8.1</version>
        </dependency>
    </dependencies>
```



**记得刷新Maven引用**

![刷新Maven引用](https://mkstatic.lianbian.net/202203151232645.png)



### 配置启动类

![配置启动类](https://mkstatic.lianbian.net/202203151235166.png)

### 指定配置

新建配置文件 `application.properties` 和 `ehcache.xml`

![application.properties内容](https://mkstatic.lianbian.net/202203151241566.png)



ehcache.xml定义一个缓存模块，指定key和value的类型，指定过期时间。

```xml
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.ehcache.org/v3"
        xmlns:jsr107="http://www.ehcache.org/v3/jsr107"
        xsi:schemaLocation="
            http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd
            http://www.ehcache.org/v3/jsr107 http://www.ehcache.org/schema/ehcache-107-ext-3.0.xsd">

    <cache alias="getUserInfo">
        <key-type>java.lang.String</key-type>
        <value-type>java.lang.String</value-type>
        <expiry>
            <ttl unit="seconds">30</ttl>
        </expiry>

        <resources>
            <heap unit="entries">2</heap>
            <offheap unit="MB">10</offheap>
        </resources>
    </cache>
</config>
```



增加配置注解config文件

![通过配置注解开启EhCache](https://mkstatic.lianbian.net/202203151243958.png)



### 模拟场景

从控制器进入查找用户，查看是否多次调用`Service`的接口。

![整体代码结构](https://mkstatic.lianbian.net/202203151258621.png)



![控制器入口](https://mkstatic.lianbian.net/202203151258300.png)

![service定义](https://mkstatic.lianbian.net/202203151259724.png)

### 场景测试

通过启动类，启动服务。

浏览器访问：http://localhost:8001/user/1

![浏览器访问结果](https://mkstatic.lianbian.net/202203151300852.png)



第一次控制台输出：

![第一次控制台输出](https://mkstatic.lianbian.net/202203151301397.png)



之后刷新没有不会有变化，过了30s之后又会重新进入方法。

### 事件监听

在`ehcache.xml`增加监听配置

```xml
<listeners>
            <listener>
                <class>com.baeldung.cachetest.config.CacheEventLogger</class>
                <event-firing-mode>ASYNCHRONOUS</event-firing-mode>
                <event-ordering-mode>UNORDERED</event-ordering-mode>
                <events-to-fire-on>CREATED</events-to-fire-on>
                <events-to-fire-on>EXPIRED</events-to-fire-on>
            </listener>
        </listeners>
```



### 测试监听

刷新浏览器测试

![第一次访问](https://mkstatic.lianbian.net/202203151308079.png)



![30秒之后刷新](https://mkstatic.lianbian.net/202203151308625.png)



注意有一个过期的事件。

## 总结

好了，`EhCache`简单的实战就到这里结束了，源码可以从 https://github.com/lianbian/EhCache 

我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203151318930.jpg)

