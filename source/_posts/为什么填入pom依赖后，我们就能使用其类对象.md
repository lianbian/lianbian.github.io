---
index_img: http://mkstatic.lianbian.net/202112282331074.png
title: 为什么填入pom依赖后，我们就能使用其类对象
keywords: java 面试
categories: java
summary: 面试官：“在SpringBoot中开发，为什么我们在填入pom依赖以后，就能使用其对象？”
author: 连边
date: 2021-12-29
tags:
  - java 面试

---



大家好，我是连边。

今天给大家一个腾讯的真实面试题。

面试官：“在SpringBoot中开发，为什么我们在填入pom依赖以后，就能使用其对象？”

今天这篇文章呢，我就来给大家分析这个问题。



## 文章导读

![自动配置](http://mkstatic.lianbian.net/202112282331074.png)



## 引用流程

当我们在`pom.xml`文件中填写引入一个依赖以后，我们的包控制器（maven、gradle）会从`settings.xml`配置的镜像地址拉取`jar`到我们本地（idea中下边的刷新按钮执行拉取），这个时候我们看到的现象就是`jar`拉取到本地不用实例化就能直接拿来用了。

![刷新引用](http://mkstatic.lianbian.net/202112282135069.png)

面试官就是问这一点，**对象我们没有注入到Spring容器中，怎么就能用了？**



## 正常的逻辑

先抛开这个问题，想想我们平常是怎样把一个`bean`对象（创建）交给Spring容器管理的？

很容易想到两种方式对不对？通过`xml配置`或者通过`@Configuration注解`。

**xml配置创建bean**

```xml
<bean id="apple" class="first.Apple"/>
```



**注解创建bean**

```java
@Configuration
public class SwaggerConfig{
    @Bean
    public Docket createRestApi()
    {
      
    }
}
```



那我们就来想一想，是不是在SpringBoot启动的时候，帮我们把这个事情自动给做了？

这里也可以想到我们Spring常说到的一句话，**约定大于配置**。

那我们去找找看看SpringBoot有没有帮我们做这件事情吧～



## 从启动过程分析

如果你还不熟悉SpringBoot启动做了什么，可以看一看我的上一篇文章 - [SpringBoot启动都做了什么？看完就懂了！](https://mp.weixin.qq.com/s/RqB_A8_yCRhFA1Vz8W47oA)

我们上篇分析了`@SpringBootApplication`是一个合成注解，分为

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

这里我们着重来看`@EnableAutoConfiguration`具体做了什么事情。

依次点击注解：`@SpringBootApplication` -> `@EnableAutoConfiguration` -> `@Import({AutoConfigurationImportSelector.class})`

点击 `AutoConfigurationImportSelector.class`

这里我用文字来详细描述该过程：

1. 利用getAutoConfigurationEntry(annotationMetadata);给容器中批量导入一些组件；
2. 调用List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes)获取到所有需要导入到容器中的配置类；
3. 利用工厂加载 Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader)；得到所有的组件；
4. 从META-INF/spring.factories位置来加载一个文件。默认扫描我们当前系统里面所有META-INF/spring.factories位置的文件，spring-boot-autoconfigure-2.5.6.RELEASE.jar包里面也有META-INF/spring.factories

虽然我们127个场景的所有自动配置启动的时候默认全部加载。xxxxAutoConfiguration
按照条件装配规则（`@Conditional`），最终会**按需配置**。



## mybatis实例讲解

![mybatis-spring-boot-autoconfigure](http://mkstatic.lianbian.net/202112282323390.png)



```java
@Configuration
// 按需加载：找得到 SqlSessionFactory.class SqlSessionFactoryBean.class类，我这个自动配置才生效。
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({MybatisProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class})
public class MybatisAutoConfiguration implements InitializingBean {

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    }
```



## autoconfigure从哪里引入的？

我在整个过程中，就是引入了这个依赖。

```xml
<!-- SpringBoot Web容器 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**`starter`的题外话：**可以把`starter`看作是`dependency`的套娃。

spring-boot-starter-web包含的`dependency`：

```xml
<dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
      <version>2.5.6</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-json</artifactId>
      <version>2.5.6</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
      <version>2.5.6</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-web</artifactId>
      <version>5.3.12</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-webmvc</artifactId>
      <version>5.3.12</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

里边还能循环嵌套～，通过一层一层的点击，找到**spring-boot-autoconfigure** 是自动配置

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
    <version>2.5.6</version>
    <scope>compile</scope>
</dependency>
```



## 总结

回到开始面试题的答案，其实有个专业名词，就是**自动配置**。

**由于我们springboot引入了`spring-boot-autoconfigure`的jar包，而在SpringBoot启动的时候自动配置注解会扫描jar包META-INF/spring.factories位置的文件，然后按需加载（通过注解实现）`xxxxxAutoConfiguration`，从而通过自动加载来实现我们看到的效果。**



### 推荐资料

最后在这里特别推荐**尚硅谷雷神**的讲SpringBoot2的视频，讲得很全面也很细致，非常推荐，有时间的朋友，可以系统的，认真的卷一下。

视频地址：https://www.bilibili.com/video/BV19K4y1L7MT

对应的笔记地址：https://www.yuque.com/atguigu/springboot/qb7hy2



## 关于我

我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203152305384.jpg)