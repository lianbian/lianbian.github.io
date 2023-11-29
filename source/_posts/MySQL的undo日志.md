---
title: MySQL的undo日志
keywords: mysql undo log
categories: mysql
summary: undo log是innodb引擎的一种日志，在事务的修改记录之前，会把该记录的原值（before image）先保存起来（undo log）再做修改，以便修改过程中出错能够恢复原值或者其他的事务读取。
author: 连边
date: 2021-10-03
tags:
  - mysql
  - log
---



大家好，我是连边。

今天这篇文章给大家带来MySQL中另外一个重要的日志 - `undo log`。

## 文章导读

![undo log文章导读](http://mkstatic.lianbian.net/202110141040653.png)

## 概念

`undo log`是innodb引擎的一种日志，在事务的修改记录之前，会把该记录的原值（before image）先保存起来（undo log）再做修改，以便修改过程中出错能够**恢复原值**或者其他的事务**读取**。

## 作用

从概念的定义不难看出`undo log`的两个作用：

1. **事务回滚 - 原子性：** undo log是为了实现事务的原子性而出现的产物，事务处理的过程中，如果出现了错误或者用户执行```ROLLBACK```语句，MySQL可以利用undo log中的备份将数据恢复到事务开始之前的状态。
2. **多个行版本控制（MVCC）- 隔离性：** undo log在MySQL InnoDB储存引擎中用来实现多版本并发控制，事务未提交之前，当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据是什么，从而提供该行版本信息，让用户实现非锁定一致性读取。

## 什么时候会生成undo log

在事务中，进行以下四种操作，都会创建`undo log`：

1. ```insert```用户定义的表
2. ```update```或者```delete```用户定义的表
3. ```insert```用户定义的临时表
4. ```update```或者```delete```用户定义的临时表

## 存放在哪里？

既然是一种日志，**储存在什么目录？** 又是**怎样储存的？**

### 储存在什么目录？

这里要需要说明一下，在```MySQL5.6.3```之前的版本中，这个`undo tablespace`是和`system tablespace`系统表空间存放在一起的，也就是没有单独的`undo log`文件，直接存放在`ibdata1`文件里边，在```MySQL5.6.3```之后的版本中，MySQL支持将undo log tablespace单独剥离出来，但这个特性依然很鸡肋：

1. 要在安装数据库的时候，就指定好独立undo tablespace，在安装完成后不可更改；
2. undo tablespace的space id必须从1开始，无法增加或者删除undo tablespace；

特意安装了`MySQL5.6.39`验证一波：

![undo tablespace表空间设置](http://mkstatic.lianbian.net/202110131612277.png)

到了`MySQL5.7`版本，终于引入期待已久的功能：即在线truncate undo tablespace（解决了第一个鸡肋点，可以在安装数据库之后更改undo tablespace）

在`MySQL8.0`中，InnoDB再进一步，对undo log做了进一步的改进：

1. **从8.0.3版本开始，默认undo tablespace的个数从0调整为2**，也就是在8.0版本中，独立undo tablespace被默认打开。修改该参数为0会报warning并在未来不再支持；
2. 无需从space_id 1开始创建undo tablespace，这样解决了In-place upgrade或者物理恢复到一个打开了Undo tablespace的实例所产生的space id冲突。不过依然要求undo tablespace的space id是连续分配的；

根据官方的MySQL结构图，我画了MySQL的结构简图，描述了undo log在数据库磁盘中的位置，只需要关注简图中画**红色方框**和**绿色方框**的模块。

![MySQL的结构简图](http://mkstatic.lianbian.net/202110121327568.png)

我们会发现，随着MySQL版本的迭代，已经把undo log单独剥离出来了，那我们思考一下：**为什么要支持把undolog的tablespace单独剥离出来呢？**

这是从性能的角度来考量的。原先的undolog和系统表空间共享一个表空间，这样在记录undolog的时候，和其他的一些使用系统表空间来存储的操作肯定会存在磁盘I/O的竞争。但是如果我们把undolog的表空间单独拉出来，支持让其自定义目录和表空间的数量，这样我们可以把undolog配置单独的磁盘目录，提高undo log日志的读写性能，也能方便DBA操作。

阅读到这里，我们弄清楚了undo log是储存在单独的undo tablespace，接下来我们继续研究undo tablespace是以什么样的结构储存日志内容的。

### undo tablespace - 表空间

在MySQL中，undo tablespace定义了回滚段 rollback segments 用来存放undo log。

我们这里来看一下undo tablespace的结构体源码。

（ps：我们还是要养成看源码的习惯，我们搜索到的知识观点很多，如何甄别观点的对与错，只有从源码层面找到答案，当然这里看MySQL源码只是为了进一步说明undo tablespace表空间定义了多个rollback segments - rseg）

我的源码版本是`8.0.13`，可以给订阅号「连边」发送指令`mysql8013`获取源码包，也可以自己在github上找对应的版本。

**unbo tablespace表空间结构体源码路径**

```mysql-server-mysql-8.0.13/storage/innobase/include/trx0purge.h```

**undo tablespace结构体定义**

```c++
/** An undo::Tablespace object is used to easily convert between
undo_space_id and undo_space_num and to create the automatic file_name
and space name.  In addition, it is used in undo::Tablespaces to track
the trx_rseg_t objects in an Rsegs vector. So we do not allocate the
Rsegs vector for each object, only when requested by the constructor. */
struct Tablespace {
 /** ... **/
 private:
  /** Undo Tablespace ID. */
  space_id_t m_id;

  /** Undo Tablespace number, from 1 to 127. This is the
  7-bit number that is used in a rollback pointer.
  Use id2num() to get this number from a space_id. */
  space_id_t m_num;

  /** The tablespace name, auto-generated when needed from
  the space number. */
  char *m_space_name;

  /** The tablespace file name, auto-generated when needed
  from the space number. */
  char *m_file_name;

  /** The tablespace log file name, auto-generated when needed
  from the space number. */
  char *m_log_file_name;

  /** List of rollback segments within this tablespace.
  This is not always used. Must call init_rsegs to use it. */
  Rsegs *m_rsegs;
};
```

从上边的源码可知，在我们的undo tablespace表空间结构体定义里边，有```Rsegs```的定义，这个就是我们前边说的```回滚段（Rollback Segments）```，我们继续从源码来研究```回滚段（Rollback Segments）```结构体。

### resg - 回滚段

**回滚段结构体源码路径**

`mysql-server-mysql-8.0.13/storage/innobase/include/trx0types.h`

**回滚段rseg结构体源码**

undo log tablespace结构体中```Rsegs```是```trx_rseg_t```的```std::vector```封装

```c++
/** The rollback segment memory object */
struct trx_rseg_t {
  /*--------------------------------------------------------*/
  /** rollback segment id == the index of its slot in the trx
  system file copy */
  ulint id;

  /** mutex protecting the fields in this struct except id,space,page_no
  which are constant */
  RsegMutex mutex;

  /** space ID where the rollback segment header is placed */
  space_id_t space_id;

  /** page number of the rollback segment header */
  page_no_t page_no;

  /** page size of the relevant tablespace */
  page_size_t page_size;

  /** maximum allowed size in pages */
  ulint max_size;

  /** current size in pages */
  ulint curr_size;

  /*--------------------------------------------------------*/
  /* Fields for update undo logs */
  /** List of update undo logs */
  UT_LIST_BASE_NODE_T(trx_undo_t) update_undo_list;

  /** List of update undo log segments cached for fast reuse */
  UT_LIST_BASE_NODE_T(trx_undo_t) update_undo_cached;

  /*--------------------------------------------------------*/
  /* Fields for insert undo logs */
  /** List of insert undo logs */
  UT_LIST_BASE_NODE_T(trx_undo_t) insert_undo_list;

  /** List of insert undo log segments cached for fast reuse */
  UT_LIST_BASE_NODE_T(trx_undo_t) insert_undo_cached;

  /*--------------------------------------------------------*/

  /** Page number of the last not yet purged log header in the history
  list; FIL_NULL if all list purged */
  page_no_t last_page_no;

  /** Byte offset of the last not yet purged log header */
  ulint last_offset;

  /** Transaction number of the last not yet purged log */
  trx_id_t last_trx_no;

  /** TRUE if the last not yet purged log needs purging */
  ibool last_del_marks;

  /** Reference counter to track rseg allocated transactions. */
  std::atomic<ulint> trx_ref_count;
};
```

每个回滚段维护了一个`Rollback Segment Header Page`，限于篇幅，这里不再深入研究，因为他不影响我们继续阅读，如果感兴趣的读者，可以看我最后贴出来的链接深入了解。

### undo tablespace 储存结构示意图

为了巩固前边说的内容，这里我画了一张undo tablespace表空间结构图，希望能帮您巩固。

![undo tablespace表空间结构图](http://mkstatic.lianbian.net/202110140914268.png)

### undo log的类型

为了更好的处理回滚，undo log和之前说的redo log记录物理日志不一样，它是逻辑日志，**可以认为当delete一条记录时，undo log中会记录一条对应的insert记录，反之亦然，当update一条记录时，它记录一条对应相反的update记录。** 对应着undo log的两种类型，分别是 `insert undo log`和`update undo log`。

**insert undo log长啥样**

对于 insert 类型的sql，会在undo log中记录下方才你insert 进来的数据的ID，根据ID完成精准的删除。

insert 类型的undo log长下面这样：

![insert undo log-不是我画的](http://mkstatic.lianbian.net/202110141001577.png)

可能你打眼一看上图就能知道各部分都有啥用。
但是，不知道你会不会纳闷这样一个问题：不是说对于insert 类型的undo log MySQL记录的是方才插入行ID吗？怎么上图整出来的了这么多Col1、Col2、Col2。
其实是MySQL设计的很周到，因为它是针对联合主键设计的。

**update undo log长啥样**

一条update sql对应undolog长如下这样：

![update undo log-不是我画的](http://mkstatic.lianbian.net/202110141002721.png)



通过上边的基础铺垫，来到我们的实战分析环节。

## 场景实战

### 事务怎么回滚的？

举一个举例的案例来说明该过程。

**insert类型的undo log**

对于insert类型的sql，会在undo log中记录下insert 进来的数据的ID，当你想roll back时，根据ID完成精准的删除。
对于delete类型的sql，会在undo log中记录方才你删除的数据，当你回滚时会将删除前的数据insert 进去。
对于update类型的sql，会在undo log中记录下修改前的数据，回滚时只需要反向update即可。
对于select类型的sql，别费心了，select不需要回滚。
先看一个简单的insert undo log 链条

![insert undo log链条-不是我画的](http://mkstatic.lianbian.net/202110141025637.png)

有一个注意点：因为单纯的insert sql不涉及多MVCC的能力。
所以一旦事务commit，这条insert undo log就可以直接删除了。

**update类型的undo log**

为了方便画图，重点突出链条的概念我省略了update undo log的部分内容
一个事物A开启后插图了一条记录：name = tom，MySQL会记录下这样一条undo log

![undo log记录-不是我画的](http://mkstatic.lianbian.net/202110141049207.png)



随后先后来了两个事物：
事物B，事物ID=61，它执行sql将name 改成jerry。
事物C，事物ID=62，它执行sql将name 改成tom。
于是MySQL记录下这样一条新的undo log

![事务执行逻辑-不是我画的](http://mkstatic.lianbian.net/202110141031889.png)

你可以看到，MySQL会将对一行数据的修改undo log通过DATA_ROLL_ID指针连接在一起形成一个undo log链表链条。这样事物C如果想回滚，他会将数据回滚到事物B修改后的状态。而事物B想回滚他会将数据回滚到事物A的状态。

### 浅谈MVCC工作原理

**undo log在事务开启之前就产生，当事务提交的时候，不会删除undo log，因为可能需要rollback操作，要执行回滚（rollback）操作时，从缓存中读取数据。InnoDB会将事务对应的日志保存在删除list中，后台通过purge线程进行回收处理。**

还是以一条sql执行update、select过程来浅析MVCC的工作原理：

执行update操作，事务A提交时候（事务还没提交），会将数据进行备份，备份到对应的undo buffer，undo log保存了未提交之前的操作日志，User表数据肯定就是持久保存到InnoDB的数据文件IBD，默认情况。

这时事务B进行查询操作，是直接读undo buffer缓存的，这时事务A还没提交事务，要回滚（rollback），是不读磁盘的，先直接从undo buffer缓存读取。

![浅析MVCC工作原理-不是我画的](http://mkstatic.lianbian.net/202110141032350.png)



## 总结

这篇文章到这里就写完了，从`undo log`概念出发，依次介绍了生成undo log、存放在哪里并且以什么方式储存的，最后结合场景实战分析了`undo log`的变化过程。

文章中使用的源码是```MySQL8.0.13```，如果有兴趣的，可以直接关注「连边」订阅号，回复```mysql8013```获取源码包。Java同学的快捷查看方式：解压源码，使用`IntelliJ IDEA`编辑器，然后根据文中的文件路径查看代码即可。

如果对文章有什么疑问或者觉得哪里不对的地方，欢迎留言或者直接加我微信跟我沟通。

衷心感谢每一位认真读文章的人，我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

![连边的个人微信号](http://mkstatic.lianbian.net/202110141058630.png)

### 参考文章

InnoDB的事务分析-Undo-Log -  https://www.leviathan.vip/2019/02/14/InnoDB%E7%9A%84%E4%BA%8B%E5%8A%A1%E5%88%86%E6%9E%90-Undo-Log/

MySQL · 引擎特性 · InnoDB undo log 漫游 - http://mysql.taobao.org/monthly/2015/04/01/

简介undo log、truncate、以及undo log如何帮你回滚事物 - https://juejin.cn/post/6900796508342321166



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202203182132192.jpg)