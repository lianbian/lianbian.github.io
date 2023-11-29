---
title: 不重启JVM替换掉已经加载的类
keywords: java
categories: java
summary: 线上就莫名其妙的出问题了。往往这个时候，我们不能中断对外提供服务，又要调试线上问题，怎么办呢？
author: 连边
date: 2021-09-07
tags:
  - java
  - arthas
---



大家好，我是连边。



## 提出问题

我们工作中，时常会碰到样的问题，测试环境、预发布环境都好好的，但是代码一到线上就莫名其妙的出问题了。往往这个时候，我们不能中断对外提供服务，又要调试线上问题，怎么办呢？

我们首先分析一下，其实我们这个问题的本质是**要动态改变内存中已存在的对象的行为**。

我们知道，JVM的操作对象是class文件，而不是源码。

所以进一步分析，**改变class文件，然后让JVM重新加载class文件就能达到我们的目的。**



## github文档

Arthas 是Alibaba开源的Java诊断工具，深受开发者喜爱。

https://github.com/alibaba/arthas/blob/master/README_CN.md



## Arthas实战

### start demo

1. 利用spring boot 在本地启动一个服务，端口：61000

```java
// demo-arthas-spring-boot.jar 关注“连边”订阅号，回复“arthas”获取jar包
java -jar demo-arthas-spring-boot.jar
```

2. 启动后如图：

![spring boot 启动图](http://mkstatic.lianbian.net/20210907220553.png)

3. 测试是否启动成功：

![image-20210907220707380](http://mkstatic.lianbian.net/20210907220707.png)

### start arthas

新开一个命令行窗口，启动arthas

```java
// 启动arthas 关注“连边”订阅号，回复“arthas”获取jar包
java -jar arthas-boot.jar
```

![输入命令行](http://mkstatic.lianbian.net/20210907220915.png)

这里选择[2]，因为[2]是我刚启动的进程

![选择指定进程](http://mkstatic.lianbian.net/20210907221107.png)

至此，准备工作完毕，第二个窗口也进入到```arthas```窗口模式。

### 热更新代码

1. 通过浏览器访问： http://localhost:61000/user/0 会抛出500的错误
2. 反编译文件到 ```/tmp/UserController.java```

```java
// jad反编译UserController并保存在 /tmp/UserController.java
jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java
```

3. 另外启动一个命令行窗口来编辑文件 ```/tmp/UserController.java```比如当 user id 小于1时，也正常返回，不抛出异常：

```java
vim /tmp/UserController.java

@GetMapping(value={"/user/{id}"})
public User findUserById(@PathVariable Integer id) {
  logger.info("id: {}", (Object)id);
  if (id != null && id < 1) {
    return new User(id, "name" + id);
    // throw new IllegalArgumentException("id < 1");
  }
  return new User(id.intValue(), "name" + id);
}
```

![编辑代码行数](http://mkstatic.lianbian.net/20210907222450.png)

4. sc查找加载UserController的classLoaderHash

```java
sc -d *UserController | grep classLoaderHash
```

![classLoaderHash](http://mkstatic.lianbian.net/20210907222520.png)

5. 通过`mc -c <classLoaderHash> /tmp/UserController.java -d /tmp`，使用`-c`参数指定ClassLoaderHash:

```java
mc -c 33c7353a /tmp/UserController.java -d /tmp
```

![mc命令行效果](http://mkstatic.lianbian.net/20210907222641.png)

6. 再使用`redefine`命令重新加载新编译好的`UserController.class`：

```java
redefine /tmp/com/example/demo/arthas/user/UserController.class
```

![redefine命令行效果](http://mkstatic.lianbian.net/20210907222757.png)

7. `redefine`成功之后，再次访问 user/0 ，结果是：

![替换之后的效果](http://mkstatic.lianbian.net/20210907222858.png)

## 总结

Java是静态语言，运行时不允许改变数据结构。

然而，Java 5引入Instrument，Java 6引入Attach API之后，事情开始变得不一样了。

虽然存在诸多限制，然而，在前辈们的努力下，仅仅是利用预留的近似于“只读”的这一点点狭小的空间，仍然创造出了各种技术，极大地提高了软件开发人员定位问题的效率。



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203182121893.jpg)