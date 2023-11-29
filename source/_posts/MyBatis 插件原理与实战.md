---
title: MyBatis 插件原理与实战
keywords: java mybatis
categories: java
summary: 希望读者朋友们在看完我写的这篇文章后，能够秒懂别人写的MyBatis插件并且能够开发出自己的MyBatis的插件。
author: 连边
date: 2022-01-25
tags:
  - java
---



## 前言

大家好，我是连边。

最近是一直在整理[#面试精选](http://mp.weixin.qq.com/mp/homepage?__biz=MzA4MjUzODc0OQ==&hid=2&sn=2004fd1d3f6bca21c4cd4783a6be7fb2&scene=18#wechat_redirect)，如果是准备最近跳槽的同学，可以点击[#面试精选](http://mp.weixin.qq.com/mp/homepage?__biz=MzA4MjUzODc0OQ==&hid=2&sn=2004fd1d3f6bca21c4cd4783a6be7fb2&scene=18#wechat_redirect)查看文章列表。

今天给大家带来MyBatis面试系列的最后一篇《MyBatis插件原理与实战》。

该系列总共4篇，4篇文章涵盖了95%的MyBatis面试题。

1. [MyBatis 常见面试题总结](https://mp.weixin.qq.com/s/Oz7fNEIKCE5axq53vdsPtA) （整体的开胃菜，快问快答系列，当作自己知识点的查漏补缺）；
2. [MyBatis 动态代理详解](https://mp.weixin.qq.com/s/L2rYzOCl7pdEce1ztN0zDg) （针对动态代码的详细解读，从Java普通代理讲起，有原理，有实战）；
3. [MyBatis 缓存工作原理](https://mp.weixin.qq.com/s/QH7p7YZIqGuMpSKF96jQMQ)
4. MyBatis插件原理与实战 （本篇）



## 概述

最近在做新项目，基于若依（前后端分离版本）做的，他也使用了常用的分页插件`PageHelper`。

老规矩，今天文章还是分三步走，先上**文章导读**，然后讲**原理**，最后讲解**源码案例**。

最后达到的效果就是希望读者朋友们在看完我写的这篇文章后，能够**秒懂别人写的MyBatis插件**并且**能够开发出自己的MyBatis的插件**。



## 文章导读

![MyBatis 插件原理与实战](http://mkstatic.lianbian.net/202201242321108.png)



## 什么是插件？

插件就是在具体的**执行流程**插一脚（触发点、拦截器）来实现具体的功能。

一般插件会对**执行流程**中的上下文有依赖，抽象的说，我们也可以把MyBatis看作是JDBC的**插件**，只是功能越来来多，越来越强大，最后我们给了他一个新名字，叫做**框架**。

不管怎样，JDBC的那一套还是不会变的，只是做了抽象、封装、归类等。

要想知道插件的原理，首先就要对它的执行流程有一定的把控。



## 执行流程

前边我们讲到，MyBatis是对JDBC的抽象、封装。

我们首先来回顾一下JDBC的执行流程。



### JDBC执行流程

1. 注册驱动；
2. 获取Connection连接；
3. 执行预编译；
4. 执行SQL；
5. 封装结果集；
6. 释放资源；

**给段伪代码通透理解下：**

```java
// 注册驱动
Class.forName("com.mysql.jdbc.Driver");
// 获取链接
Connection con = DriverManager.getConnection(url, username, password);
// 执行预编译
Statement stmt = con.createStatement();
// 执行SQL
ResultSet rs = stmt.executeQuery("SELECT * FROM ...");
// 封装结果
while (rs.next()) {
  String name = rs.getString("name");
  String pass = rs.getString(1); // 此方法比较高效
}
// 释放资源
if (rs != null) {  // 关闭记录集

}
if (stmt != null) {  // 关闭声明

}
if (conn != null) { // 关闭连接对象

}
```

上边的代码是不是很熟悉，我相信每个入门写Java代码的人，都写过这段代码。

紧接着，我们继续来了解MyBatis的执行流程。



### MyBatis执行流程

1. 读取MyBatis的核心配置文件；

2. 加载映射文件；
3. 构造会话工厂获取SqlSessionFactory；
4. 创建会话对象SqlSession；
5. Executor执行器；
6. MappedStatement对象；
7. 输入参数映射；
8. 封装结果集；

上边的文字可能不太好理解，我这里也画一幅执行流程图，来方便理解。

![MyBatis执行流程](http://mkstatic.lianbian.net/202201242305818.png)



有没有觉得MyBatis的执行流程和JDBC的执行流程主干也差不多，只是在主干过程中，把一些配置（mybatis-config.xml）、常用的定义文件单独抽离出来（mapper.xml）和一些附带扩展性的**拦截点**抽离了出来。

下面着重讲一讲我们的**拦截点**，因为插件就是基于我们的**拦截点**来做的扩展。



## 拦截点

结合上边的MyBatis执行流程，看下图的各个拦截点：

![MyBatis拦截点](http://mkstatic.lianbian.net/202201242311617.png)



文字描述，MyBatis允许使用插件来**拦截的方法调用**包括：

1. Executor：

   拦截执行器的方法 (**update, query, flushStatements, commit, rollback,getTransaction, close, isClosed**)，Mybatis的内部执行器，它负责调用StatementHandler操作数据库，并把结果集通过 ResultSetHandler进行自动映射，另外，他还处理了二级缓存的操作。从这里可以看出，我们也是可以通过插件来实现自定义的二级缓存的；

2. ParameterHandler：

   拦截参数的处理 (**getParameterObject, setParameters**) ，Mybatis直接和数据库执行sql脚本的对象。另外它也实现了Mybatis的一级缓存。这里，我们可以使用插件来实现对一级缓存的操作(禁用等等)；

3. ResultSetHandler：

   拦截结果集的处理 (**handleResultSets, handleOutputParameters**) ，Mybatis实现Sql入参设置的对象。插件可以改变我们Sql的参数默认设置；

4. StatementHandler：

   拦截Sql语法构建的处理 (**prepare, parameterize, batch, update, query**) ，Mybatis把ResultSet集合映射成POJO的接口对象。我们可以定义插件对Mybatis的结果集自动映射进行修改。



## 拦截器为什么能够拦截

`org.apache.ibatis.session.Configuration`是在MyBatis初始化配置的类。

其中的`newParameterHandler`、`newResultSetHandler`、`newStatementHandler`、`newExecutor`这几个方法在创建指定的对象（newParameterHandler创建ParameterHandler、newResultSetHandler创建ResultSetHandler、newStatementHandler创建StatementHandler、newExecutor创建Executor）对象的时候，都会调用一个统一的方法：

![创建对象](http://mkstatic.lianbian.net/202201242215206.png)

这4个方法实例化了对应的对象之后，都会调用interceptorChain的pluginAll方法，那么下面我们在来看pluginAll干了什么。

包路径：`org.apache.ibatis.plugin.InterceptorChain`

```java
public Object pluginAll(Object target) {
        Interceptor interceptor;
        for(Iterator var2 = this.interceptors.iterator(); var2.hasNext(); target = interceptor.plugin(target)) {
            interceptor = (Interceptor)var2.next();
        }

        return target;
    }
```

原来这个pluginAll方法就是遍历所有的拦截器，然后顺序执行我们插件的plugin方法,一层一层返回我们原对象(Executor/ParameterHandler/ResultSetHander/StatementHandler)的代理对象。当我们调用四大接口对象的方法时候，实际上是调用代理对象的响应方法，代理对象又会调用四大接口对象的实例。

这里我们看到所有的拦截器Interceptor，其实它和我们平常写代码一样，也是多态的利用，存在一个拦截器Interceptor接口，**我们在实现插件的时候，也实现这个接口，就会被调用。**



## Interceptor接口

包路径：`org.apache.ibatis.plugin`

```java
public interface Interceptor {
    Object intercept(Invocation var1) throws Throwable;

    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
    }
}
```

这个接口只声明了三个方法：

1. setProperties方法是在Mybatis进行配置插件的时候可以配置自定义相关属性，即：接口实现对象的参数配置；
2. plugin方法是插件用于封装目标对象的，通过该方法我们可以返回目标对象本身，也可以返回一个它的代理，可以决定是否要进行拦截进而决定要返回一个什么样的目标对象，官方提供了示例：return Plugin.wrap(target, this)；
3. intercept方法就是要进行拦截的时候要执行的方法；



## 编写简单的MyBatis插件

**注：MyBatis默认没有一个拦截器接口的实现类，开发者可以实现符合自己需求的拦截器**



```java
@Intercepts({@Signature(type= Executor.class, method = "update", args = {MappedStatement.class,Object.class})})
public class ExamplePlugin implements Interceptor {
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    public void setProperties(Properties properties) {
    }
}
```

**全局xml配置（实例化bean）**

```xml
<plugins>
<plugin interceptor="org.format.mybatis.cache.interceptor.ExamplePlugin"></plugin>
</plugins>
```



**这个拦截器拦截Executor接口的update方法（其实也就是SqlSession的新增，删除，修改操作），所有执行executor的update方法都会被该拦截器拦截到，就在里边做相对应的逻辑处理就可以了。**



## 总结

今天这篇文章到这里结束了，讲解了什么是插件首先需要了解执行流程，然后回顾我们的JDBC流程来推导出MyBatis的执行流程，通过初始化的`org.apache.ibatis.session.Configuration`为切入点，跟踪到interceptorChain的pluginAll方法；最后通过一个简单的插件来实操了一波。

参考链接：

https://zhuanlan.zhihu.com/p/150008843

https://blog.csdn.net/weixin_44046437/article/details/100523028

https://blog.csdn.net/weixin_44046437/article/details/100526643



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203160755986.jpg)