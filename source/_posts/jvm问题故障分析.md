---
title: jvm问题故障分析
keywords: jvm
categories: java
summary: 系统就会变得缓慢，需要重启服务才能恢复访问
author: 连边
date: 2021-08-02
tags:
  - java jam
---



## 问题现象

jar能正常启动，但是到下午四点的时候，系统就会变得缓慢，需要重启服务才能恢复访问，cpu和内存占用都不高。

查看日志：

```shell
```

spring启动注册的bean被销毁了。



## 分析问题

最近我们系统做了什么改动？

原来稳定的系统，为什么会突然变得不稳定。查看代码，看看是否有嫌疑代码，没有找到，再想有什么改动 ... 想到了最近做的服务化部署，经过一番查看，找到了原因。

黑窗口启动：```xxx-jar-start.bat```

```shell
```

服务化后的启动参数xml（粗心导致的）

```xml
```





## 原理分析

1. 虚拟机类型
2. 分代收集理论设计
3. jvm内存模型
4. 搞清楚jvm几个参数含义
5. 32位Java默认启动参数

```shell
## 简单参数
[root@localhost ~]# java -XX:+PrintCommandLineFlags -version
## 详细参数
[root@localhost ~]# java -XX:+PrintFlagsFinal -version
## 过滤参数
[root@localhost ~]# java -XX:+PrintFlagsFinal -version | grep MaxHeapSize
```

6. 复现问题
7. jvm内存分布情况
8. 增加GC日志

