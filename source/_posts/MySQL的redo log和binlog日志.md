---
title: MySQL的redo log和binlog日志
keywords: mysql redo log binlog
categories: 数据库
summary: 今天这篇文章给大家带来MySQL中重要的两个日志 - redo log、binlog，从理论概念出发，结合图解分析，看完这篇文章之后，你能对redo log、binlog有深入的理解。
author: 连边
date: 2021-09-09
tags:
  - mysql
---



大家好，我是连边。

今天这篇文章给大家带来MySQL中重要的两个日志 - ```redo log、binlog```，从理论概念出发，结合图解分析，看完这篇文章之后，你能对```redo log、binlog```有深入的理解。

## 文章框架图

![MySQL两个日志](http://mkstatic.lianbian.net/20210915164447.png)

## 浅谈MySQL分层架构

在讲具体的日志之前，先稍微铺垫下MySQL分层的架构，让大家知道```redo log、binlog```是由MySQL的哪一层产生的。

![MySQL分层架构图](http://mkstatic.lianbian.net/20210915095746.png)

Mysql整体分为3层：客户端层，Server层和存储引擎层。我们的binlog日志，由Server层生成，redo log是innodb特有的日志，由innodb引擎生成。

## 重做日志（redo log）

### 什么是redo log

innodb为了能够支持事务一系列操作，而事务有4种特性：```原子性、一致性、隔离性、持久性```，在事务操作中，要么全部执行，要么全部不执行，这就是事务的目的。而我们的redo log用来保证事务的持久性，即我们常说的ACID中的D。我们只需要知道它是通过一套什么样的机制，来保证持久性，就能掌握好redo log。

这里的说的持久性，是说最后落盘到redo log文件中（即常见的```ib_logfile```文件），因为最后我们异常情况的恢复，都是根据文件来做恢复的。

MySQL innodb是通过一套什么样的机制，来确保**速度**与**redo log的可靠性**的呢？

### WAL

在计算机体系中，CPU处理速度和硬盘的速度，是不在同一个数量级上的，为了让它们速度匹配，从而催生了我们的内存模块，但是内存有一个特点，就是掉电之后，数据就会丢失，不是持久的，我们需要持久化的数据，最后都需要存储到硬盘上。

innodb引擎设计者也利用了类似的设计思想，先写内存，再写硬盘，这样子就不会因为redo log而导致数据库性能问题。而在innodb中，这种技术有一个专业名称，叫做**Write-Ahead Log（预先日志持久化）**



![先写buffer 再写磁盘](http://mkstatic.lianbian.net/20210915103820.png)



### redo log写入策略

上边是保证了处理的速度，但是怎么样保证写入到硬盘的可靠性呢？

InnoDB引擎的设计者也设计了一种写入的策略，首先有一个后台线程，每隔1秒，就会把```redo log buffer```中的日志，调用write写到文件系统的```page cache```，然后调用```fsync```持久化到磁盘（即redo log文件 ```ib_logfile0 ib_logfile1 ```）。

为了控制 redo log写入策略，InnoDB提供了```innodb_flush_log_at_trx_commit```配置参数，它有三种取值：

1. 设置为 0 的时候，表示每次事务提交时都只是把 redo log 留在 redo log buffer 中 ;
2. 设置为 1 的时候，表示每次事务提交时都将 redo log 直接持久化到磁盘；
3. 设置为 2 的时候，表示每次事务提交时都只是把 redo log 写到 page cache。



**如果不是对性能要求高的，一般把该参数设置为 1**



### redo log的擦除

通过上边的设计，**速度**和**可靠性**的问题都解决了，但是我们仔细想想，还会有什么问题？

随着文件的增加，落盘的速度会越来越慢，直到有一天 ... 

聪明的设计者这样子想着，如果我一直处理小文件，最大不能超过某个大小，不就行了？

也确实是这样子处理的，但是这里就涉及到一个删除日志文件的算法，即我们的**redo log擦除**。

redo log 的大小是固定的，比如可以配置一组4个文件，每个文件大小是8M，那么这个redo log总共就可以记录32M的操作，这个参数可以通过```innodb_log_file_size```设置。

下图是具体的擦除算法，ib_logfile 从头开始写，写到末尾就又回到开头循环写。

![擦除示意图 - 来自丁奇MySQL连边编辑](http://mkstatic.lianbian.net/20210910212751.png)

write pos是当前记录的位置，一边写一边后移，写到第3号文件末尾后就回到0号文件开头。checkpoint是当前要擦除的位置，也是往后移动并且循环的，擦除记录前要把记录更新到数据文件，write pos与check point之间为剩余可用写入的空间。

何时会擦除redo log并更新到数据文件中

1. 系统空闲时
2. Redo log文件没有空闲空间时，即write pos追上check point的时候；
3. MySQL Server正常关闭时

### crash-safe

有了以上这一些机制保障，我们可以相信redo log是可靠的，只要持久化到redo log文件中了，InnoDB 就可以保证即使数据库发生异常重启，之前提交的记录都不会丢失，而我们把这个能力称为 ```crash-safe```。



## 归档日志（binlog）

在写这篇文章的时候，纠结到底先写redo log还是binlog，最后还是秉承**先苦后甜**的原则，把redo log写在前面了。如果redo log的部分看懂了，binlog掌握是轻松的，跟着我的思路，我们继续binlog～

前边讲过，redo log是InnoDB引擎特有的日志，是引擎层面的日志，而在我们的数据库的Server层面，也有自己的日志，称为binlog（归档日志）。

binlog是逻辑日志，怎么样来理解这个**逻辑日志**呢？

我们通过查看一段binlog来理解。

### 理解逻辑日志

**这里一大段的操作，都是为了查看binlog文件里边存储的是什么内容，熟悉的读者可以直接略过。**

执行命令，写入新binlog文件，不让之前的逻辑影响。

执行一次flush logs命令行，就会在data目录下新增一个mysql-bin.00000x文件

```shell
## 登陆MySQL命令行
mysql -uroot -p
## 刷新binlog
flush logs;
## 确认刷新binlog成功
show master status;
## 查询binlog日志位置
 show variables like'log_bin%';
```

![data目录下的mysql-bin文件](http://mkstatic.lianbian.net/20210915113418.png)

测试数据

```sql
## 创建表
CREATE TABLE `User`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) CHARACTER SET gb2312 COLLATE gb2312_chinese_ci NOT NULL,
  `age` int(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

## 新增
INSERT `User` VALUES("1", "张三", 18);
INSERT `User` VALUES("2", "李四", 20);
## 修改
DELETE FROM `User` WHERE id = 1;
```

![执行语句截图](http://mkstatic.lianbian.net/20210915143958.png)

翻译binlog二进制文件

```shell
sudo /usr/local/mysql/bin/mysqlbinlog --base64-output=DECODE-ROWS -v mysql-bin.000006 > mysqlbin.sql
```

![binlog翻译](http://mkstatic.lianbian.net/20210915144240.png)



这是翻译出来的sql文件，是因为我在```mysqlbinlog -v```参数加工而成的。

**由此可知，逻辑日志里边就是记录着sql语句，通过sql语句记录着逻辑的变化，比如insert, update等动作，但不是记录具体数据，那个由物理日志完成。**

### 与redo log的区别

1. redo log是innoDB引擎特有的；binlog是MySQL的Server层实现的，所有引擎都能使用；
2. redo log是循环写的，空间固定会用完；binlog是追加写入的。“追加写”是指binlog文件写到一定大小后会切换到下一个，并不会覆盖以前的日志。



### binlog写入策略

通过与redo log的区别，我们知道，binlog是追加写入的，所以与redo log写入相比，没有擦除的概念。那么，还有一些什么样的其他的区别呢？

binlog的写入逻辑比较简单：事务执行过程中，先把日志写到binlog cahce，事务提交的时候，再把binlog cache写到binlog文件中（落盘）。

![binlog写入](http://mkstatic.lianbian.net/20210915213519.png)

从上图可以看到，每个线程都有自己的binlog cache，但是共用同一份binlog文件。

图中的write，指的就是把日志写入到围巾啊系统的page cache，并没有把数据持久化到磁盘，所有速度很快；

途中的sync，才是将数据持久化到磁盘的操作。

write 和 fsync 的时机，是由参数 sync_binlog 控制的：

sync_binlog=0 的时候，表示每次提交事务都只 write，不 fsync；

sync_binlog=1 的时候，表示每次提交事务都会执行 fsync；

sync_binlog=N(N>1) 的时候，表示每次提交事务都 write，但累积 N 个事务后才 fsync。



>因此，在出现 IO 瓶颈的场景里，将 sync_binlog 设置成一个比较大的值，可以提升性能。在实际的业务场景中，考虑到丢失日志量的可控性，一般不建议将这个参数设成 0，比较常见的是将其设置为 100~1000 中的某个数值。
>
>但是，将 sync_binlog 设置为 N，对应的风险是：如果主机发生异常重启，会丢失最近 N 个事务的 binlog 日志。
>
>引用《极客时间MySQL45讲》



## 浅谈两阶段提交

这里讲的两阶段提交，就是纯粹的指redo log和binlog日志的两阶段提交。

而两阶段提交的目的就是让redo log和binlog两个日志逻辑上一致。

如果redo log持久化并进行了提交，而binlog未持久化数据库就crash了，则从库从binlog拉取数据会少于主库，造成不一致。因此需要内部事务来保证两种日志的一致性。

### 两阶段提交步骤

![两阶段提交](http://mkstatic.lianbian.net/20210915171349.png)

1. 将语句执行
2. 记录redo log，并将记录状态设置为prepare
3. 通知Server，已经修改好了，可以提交事务了
4. 将更新的内容写入binlog
5. commit，提交事务
6. 将redo log里这个事务相关的记录状态设置为commited



**prepare：**redolog写入log buffer，并fsync持久化到磁盘，在redolog事务中记录2PC的XID，在redolog事务打上prepare标识
**commit：**binlog写入log buffer，并fsync持久化到磁盘，在binlog事务中记录2PC的XID，同时在redolog事务打上commit标识
其中，prepare和commit阶段所提到的“事务”，都是指内部XA事务，即2PC

### 恢复步骤

redolog中的事务如果经历了二阶段提交中的prepare阶段，则会打上prepare标识，如果经历commit阶段，则会打上commit标识（此时redolog和binlog均已落盘）。

1. 按顺序扫描redolog，如果redolog中的事务既有prepare标识，又有commit标识，就直接提交（复制redolog disk中的数据页到磁盘数据页）
2. 如果redolog事务只有prepare标识，没有commit标识，则说明当前事务在commit阶段crash了，binlog中当前事务是否完整未可知，此时拿着redolog中当前事务的XID（redolog和binlog中事务落盘的标识），去查看binlog中是否存在此XID
   1. 如果binlog中有当前事务的XID，则提交事务（复制redolog disk中的数据页到磁盘数据页）
   2. 如果binlog中没有当前事务的XID，则回滚事务（使用undolog来删除redolog中的对应事务）

可以将mysql redolog和binlog二阶段提交和广义上的二阶段提交进行对比，广义上的二阶段提交，若某个参与者超时未收到协调者的ack通知，则会进行回滚，回滚逻辑需要开发者在各个参与者中进行记录。mysql二阶段提交是通过xid进行恢复。



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203182128339.jpg)
