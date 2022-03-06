---
img: http://mkstatic.lianbian.net/202112062123673.png
title: Win下部署多个MySQL数据库实例
keywords: mysql
categories: mysql
summary: 在同一台服务器上部署多个`MySQL`实例的操作过程。
author: 连边
date: 2021-12-06
tags:
  - mysql
---

大家好，我是连边。

当你们考虑项目并发的时候，我在部署环境，当你们在纠结使用`ArrayList`还是`LinkedArrayList`的时候，我还是在部署环境。所以啊，**技术不止境，我在部环境。**

今天这篇文章缕一下在同一台服务器上部署多个`MySQL`实例的操作过程，就是一篇工作手记，没有高深的内容，希望需要用到的朋友，按照这篇文章操作，能够**不踩坑**的把实例部署好。



## 文章导读

![Win下部署多个MySQL数据库实例](http://mkstatic.lianbian.net/202112062123673.png)



## 下载

我这里以安装`5.7.17`版本，可以到官网下载对应的版本。

**如果配置了`mysql`相关的环境变量，请删除原来的环境变量，再进行安装。**



## 解压&&目录

解压之后，目录如下：

![MySQL目录](http://mkstatic.lianbian.net/202112061915202.png)



## 开始安装

### 重命名解压文件夹

把`mysql5.7.17-winx64`重命名成`mysql5.7.17-3308`

### 配置端口

**切换到解压文件夹**

```shell
cd softs/mysql5.7.17-3308
```

找到`my-default.ini`并重命名配置文件：`my-default.ini -> my.ini`

配置端口节点：

```shell
# prot = .....
prot = 3306
```

![修改端口](http://mkstatic.lianbian.net/202112061946359.png)

### 以**管理员**方式运行命令行窗口

**如果配置了`mysql`相关的环境变量，请删除原来的环境变量，再进行安装。**

也可以使用快捷键，`Win + R`，！！注意要以**管理员的身份运行命令行窗口**

**进入到 bin 目录**

`cd bin`

**初始化 | 会在数据库的根目录上创建 data 文件夹**

`mysqld --initialize --console`

**查看默认密码**

![默认密码](http://mkstatic.lianbian.net/202112061950528.png)

**安装服务 | mysqld --install 服务名称**

`mysqld --install MySQL5.7-3308`

**启动服务**

`net start MySQL5.7-3308`

![注册&启动服务](http://mkstatic.lianbian.net/202112061958557.png)

至此，如果一切正常，其实就是这三步，就安装好了一个MySQL数据库实例。

**查看服务**

命令行执行：`services.msc`

![查看服务](http://mkstatic.lianbian.net/202112062015514.png)

我们接着测试该数据库的其可用性。



### 测试链接

命令行窗口和客户端工具连接都可以。

`mysql -uroot -P3308 -p`

密码输入在`mysqld --initialize --console`步骤生成的密码。

![默认密码](http://mkstatic.lianbian.net/202112062117241.png)

如果用客户端连接这个时候会有一个警告：`Your password has expired. To log in you must  change it using a client that supports expired passwords.`

翻译过来大概意思就是：“您的密码已过期。要登录，您必须使用支持过期密码的客户端更改它”

解决办法：root权限登录mysql：`mysql -uroot -p`

登录成功后修改密码：`set password=password('root');`

### 安装第二个数据库

重复以上的步骤，保证端口不被占用，不重复就可以了。

这里不再赘述，有问题的可以欢迎加我微信交流。

### 一些常用的其他命令

```shell
## 根据端口号找PID
netstat -aon|findstr "3308"
## 查看指定PID
tasklist|findstr "9088"
## 停止服务
net stop MySQL5.7-3306
## 卸载服务（只有在服务停止的时候才能卸载）
mysqld remove MySQL5.7-3306
```



## 总结

一篇手记，没有啥好总结的，就是希望大家伙少踩坑。

我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。