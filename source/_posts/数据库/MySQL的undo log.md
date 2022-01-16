---
title: MySQL的undo log
date: 2021-10-03
---



大家好，我是连边，这是我的第```25```篇原创文章。

今天这篇文章给大家带来MySQL中另外一个重要的日志 - `undo log`。

## 文章导读

![undo log文章导读](http://mkstatic.lianbian.net/202110121635854.png)

## 概念与作用

### 概念

一条undo log对应一个事务中的一条读写语句，在修改记录之前，把该记录的原值（before image）先保存起来（undo log）再修改。以便其他的事务**读取**或者修改过程中**出错能够恢复**原值。undo是逻辑日志，只是将数据库逻辑地恢复到原来的样子；所有修改都被逻辑地取消了，但是数据结构和页本身在回滚之后可能不大相同。

### 作用

1. **事务回滚 - 原子性：**undo log是为了实现事务的原子性而出现的产物，事务处理的过程中，如果出现了错误或者用户执行```ROLLBACK```语句，MySQL可以利用undo log中的备份将数据恢复到事务开始之前的状态。
2. **多个行版本控制（MVCC）- 隔离性：**undo log在MySQL InnoDB储存引擎中用来实现多版本并发控制，事务未提交之前，当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据是什么，从而提供该行版本信息，让用户实现非锁定一致性读取。



## 触发点

事务中的四种操作会产生```undo log```：

1. ```insert```用户定义的表
2. ```update```或者```delete```用户定义的表
3. ```insert```用户定义的临时表
4. ```update```或者```delete```用户定义的临时表

**对数据有变动的语句，都会记录到undo log。**

## undo log的储存空间

MySQL中的数据存放有对应的表空间，日志的存放也有对应的表空间，一个个表空间对应到磁盘上就是一个个数据文件。undolog也有undolog tablespace的概念，但是undo的表空间在不同的MySQL版本中有一点不一样，接下来分别说明一下。

### MySQL结构简图

我画了MySQL的结构简图，这里我们只需要关注简图中画**红色方框**和**绿色方框**的模块。

![MySQL的结构简图](http://mkstatic.lianbian.net/202110121327568.png)

在```MySQL5.6.3```之前的版本中，这个undo tablespace是和system tablespace系统表空间存放在一起的，如上图**红色框**选的部分，因为系统表空间对应的磁盘上面的数据文件就是```ibdata1```这个文件，所以在MySQL的相关目录下面，我们看不到任何相关的数据文件 - **和系统表空间存放在一起**；在```MySQL5.6.3```之后的版本中，MySQL支持将undo log tablespace单独剥离出来，不再和系统空间放在一起，如上图**绿色框**选的部分 - **独立的undo log  tablespace**。

### 独立undo tablespaces的意义

**我们思考一下：为什么要支持把undolog的tablespace单独剥离出来呢？**

这是从性能的角度来考量的。原先的undolog和系统表空间共享一个表空间，这样在记录undolog的时候，和其他的一些使用系统表空间来存储的操作肯定会存在磁盘I/O的竞争。但是如果我们把undolog的表空间单独拉出来，支持让其自定义目录和表空间的数量，这样我们可以把undolog配置单独的磁盘目录，提高undo log日志的读写性能。

## undo tablespace创建流程

undo tablespace（undo 表空间） 定义了回滚段（Rollback Segments）用来存放undo log，至于什么是回滚段，文章的后边会详细的说，我们先从undo表空间创建流程入手，undo表空间默认的最小数量是2个，在MySQL初始化时创建。

### undo tablespace初始化

undo tablespace的起始 space id 是4294967154, 支持最大的 Undo表空间个数为127个, 所以终止 space id 为4294967280。

Undo表空间通过`srv_undo_tablespace_create()`创建，并默认分配```UNDO_INITIAL_SIZE_IN_PAGES```(16MB) 大小的空间。

### 服务启动源码文件路径

```c++
mysql-server-mysql-8.0.13/storage/innobase/srv/srv0start.cc
```

### 创建undo表空间图示流程

![创建undo表空间图示流程](http://mkstatic.lianbian.net/202110120957743.png)

### 创建undo表空间源码流程

```c++
 -------------
| srv_start() |
 -------------
   |
   |    /* 初始化 Undo Tablespace */
   |    -----------------------------
   --> | srv_undo_tablespaces_init() |
        -----------------------------
         |
         |     /* 创建默认数量的 Undo Tablespace, 并创建文件 undo_xxx */
         |    -------------------------------
         --> | srv_undo_tablespaces_create() |
         |    -------------------------------
         |
         |     /* 初始化 Undo Tablespace 文件结构 */
         |    --------------------------------------
         --> |   srv_undo_tablespaces_construct()   |
              --------------------------------------
                |
                |
                |    /* 初始化 Undo Tablespace 的文件结构 Header */
                |    -------------------
                --> | fsp_header_init() |
                |    -------------------
                |
                |    /* 创建回滚段目录Page */
                |    -------------------------
                --> | trx_rseg_array_create() |
                     -------------------------
```

## undo表空间结构体

### 结构体定义源码文件路径

```mysql-server-mysql-8.0.13/storage/innobase/include/trx0purge.h```

### 结构体定义源码

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

从上边的源码可知，在我们的undo表空间结构体定义里边，有```Rsegs```的定义，这个就是我们前边说的```回滚段（Rollback Segments）```，我们继续从源码来深挖```回滚段（Rollback Segments）```结构体。

## 回滚段创建流程

每个undo表空间中有128个回滚段，每个回滚段用来管理undo log，每个回滚段维护了一个 Rollback Segment Header Page，在默认16KB的情况下，回滚段Header Page划分了1024个undo slots，每个undo slot对应一个undo log segment对象，即事务启动时分配的undo log空间，回滚段的内存数据结构是```try_rseg_t```，结构体中```Rsegs```是```trx_rseg_t```的```std::vector```封装。在 DB init 阶段初始化undo表空间后依次为每个undo表空间创建128个回滚段。

### 创建回滚段图示流程

![创建回滚段图示流程](http://mkstatic.lianbian.net/202110121107961.png)

### 创建回滚段源码流程

```c++
/* DB init */
 -------------
| srv_start() |
 -------------
  |
  |               /* 添加回滚段 */
  |    -------------------------------------
  --> | trx_rseg_adjust_rollback_segments() |
       -------------------------------------
         |
         |
         |              /* 创建回滚段 */
         |    ----------------------------------
         --> | trx_rseg_add_rollback_segments() |
              ----------------------------------
                |
                |     /* 创建回滚段文件结构 */
                |    -------------------------
                --> |    trx_rseg_create()    |
                |    -------------------------
                |       |
                |       | 
                |       |       /* 创建回滚段Header */
                |       |    ---------------------------
                |       --> |  trx_rseg_header_create() |
                |            ---------------------------
                |
                |     /* 创建并初始化trx_rseg_t */
                |    -----------------------------
                --> |    trx_rseg_mem_create()    |
                     -----------------------------
```

### 创建回滚段文字描述

1. 为指定的undo表空间创建回滚段，这里的每一个回滚段申请file segment，可以理解为一个回滚段对应一个文件形式的segment
2. 每个 Undo Tablespace 默认创建128个回滚段, Segment 创建成功后返回的 File Segment Header Page 作为 Rollback Segment Header Page, 并初始化 Rollback Segment Header Page 中的`TRX_RSEG_MAX_SIZE`,`TRX_RSEG_HISTORY_SIZE`和文件链表`TRX_RSEG_HISTORY`. 初始化 Rollback Segment Header 的 Undo Slots 字段为`FIL_NULL`, 一个回滚段默认1024个 Undo Log Segment.
3. 获取 Undo Tablespace 的回滚段目录的 Page, Rollback Segment Directory Header Page 固定为 Undo Tablspace 的第 3 (FSP_RSEG_ARRAY_PAGE_NO) 个Page, 页内偏移为`RSEG_ARRAY_HEADER`. 将创建的 Rollback Segment Header 的 Page No 插入 Undo Tablespace 中的回滚段目录(`trx_rsegsf_set_page_no()`).
4. 创建回滚段内存结构`trx_rsegs_t`并插入 Undo Tablespace 的`Rsegs`.

## 回滚段结构体

### 结构体定义源码文件路径

```c++
mysql-server-mysql-8.0.13/storage/innobase/include/trx0types.h
```

### 结构体定义源码

结构体中```Rsegs```是```trx_rseg_t```的```std::vector```封装

每个回滚段维护了一个 Rollback Segment Header Page

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

以上说了这么多篇幅，其实就是为了弄懂**undo表空间的储存结构体**，我在这里再总结一张undo表空间结构图，希望能够帮您巩固。

## undo表空间结构图

![undo tablespaces储存示意图](http://mkstatic.lianbian.net/202110121538553.png)



如果上边没有弄懂的，建议再看一遍，如果看懂了，可以继续**往下阅读**。

上边我们详细讲了undo表空间的储存结构体，为了保证事务并发操作时，在写各自的undo log时不产生冲突，InnoDB采用回滚段的方式来维护undo log的并发写入和持久化。

下面我们接着来讲**事务的执行流程**。

## 事务流程

### 分配回滚段

当开启一个读写的事务时候，我们需要为其分配一个回滚段空间，需要注意的是一个回滚段并不是一个事务独占的，回滚段申请流程如下：

**分配回滚段源码流程**

```c++
     /* 分配回滚段 */
 ----------------------------
| trx_assign_rseg_durable()  |
 ----------------------------
                          |
                          |
                          |   ----------------------
                          -> | get_next_redo_rseg() |
                              ----------------------
                                                  |
                                                  |   
                                                  |   -----------------------------------
                                                  -> | get_next_redo_rseg_from_trx_sys() |
                                                  |   -----------------------------------
                                                  |
                                                  |   ---------------------------------------
                                                  -> | get_next_redo_rseg_from_undo_spaces() |
                                                      ---------------------------------------
```

**分配回滚段文字描述**

通过判断`trx_sys->rsegs`是否为空，假如不为空则直接从`trx_sys->rsegs`获取(从`trx_sys->rsegs`中取模迭代获取)，否则从 Undo Tablespace 中获取(`get_next_redo_rseg_from_undo_spaces()`):

**迭代方式源码文件路径**

```c++
mysql-server-mysql-8.0.13/storage/innobase/trx/trx0trx.cc
```

采用轮询的方式获取回滚段，迭代方式如下：

```c++
while (rseg == nullptr) {
    /* Traverse the rsegs like this: (space, rseg_id)
    (0,0), (1,0), ... (n,0), (0,1), (1,1), ... (n,1), ... */
    ulint window =
        current % (target_rollback_segments * target_undo_tablespaces);
    ulint spaces_slot = window % target_undo_tablespaces;
    ulint rseg_slot = window / target_undo_tablespaces;
    /* 这里省略，篇幅太长了，可以关注「连边」订阅号，发送 mysql0813 获取mysql源码根据源码路径查看*/
  }
```

分配回滚段成功后, 递增`rseg->trx_ref_count`，并由`trx->rsegs.m_redo.rseg`指向分配的回滚段递增`rseg->trx_ref_count`

### 使用回滚段

我们以insert操作举例，insert一条记录的流程如下：

**使用回滚段源码流程**

```c++
 ----------------
| ha_write_row() |
 ----------------
   |
   |   --------------------------
   -> | ha_innobase::write_row() |
       --------------------------
        |
        |   ------------------------
        -> | row_insert_for_mysql() |
            ------------------------
              |
              |   ----------------------------------------
              -> | row_insert_for_mysql_using_ins_graph() |
                  ----------------------------------------
                     |
                     |   ----------------
                     -> | row_ins_step() |
                         ----------------
                            |
                            |   -----------
                            -> | row_ins() |
                                -----------
                                   |
                                   |   ----------------------------
                                   -> | row_ins_index_entry_step() |
                                       ----------------------------
                                          |
                                          |   -----------------------
                                          -> | row_ins_index_entry() |
                                              -----------------------
                                                 |
                                                 |     /* 假如插入的 Record 为聚簇索引. */
                                                 |     -----------------------------
                                                 ---> | row_ins_clust_index_entry() |
                                                 |     -----------------------------
                                                 |
                                                 |     /* 假如插入的 Record 非聚簇索引，但为多个value. */
                                                 |     ---------------------------------------
                                                 ---> | row_ins_sec_index_multi_value_entry() |
                                                 |     ---------------------------------------
                                                 |
                                                 |     /* 假如插入的 Record 为二级索引的单个value. */
                                                 |     ---------------------------
                                                 ---> | row_ins_sec_index_entry() |
                                                       ---------------------------
```

我们以插入一条聚簇索引的 Record 为例，`row_ins_clust_index_entry()`调用`row_ins_clust_index_entry_low()`实现具体的 Record 插入操作，下面是代码流程：

```c++
 ---------------------------------
| row_ins_clust_index_entry_low() |
 ---------------------------------
    |
    |   -----------------------------
    -> | btr_cur_optimistic_insert() |
        -----------------------------
           |
           |   -----------------------------
           -> | btr_cur_ins_lock_and_undo() |
               -----------------------------
                  |
                  |    /* 对DML操作记录Undo Log */
                  |   ---------------------------------
                  -> | trx_undo_report_row_operation() |
                      ---------------------------------
```

`btr_cur_ins_lock_and_undo()`检查相关的 lock 并根据事务决定是否记录 Undo Log, 假如需要记录undo log而`trx_undo_report_row_operation()`根据DML类型例如`update`, `insert`或者`delete`进行写undo log的操作。

### 写入undo log

在事务启动时，我们为其分配了回滚段, 在`trx_undo_report_row_operation()`即真正写入undo log的操作中，我们需要为事务申请 Undo Log(trx_undo_assign_undo())，**对于临时表记录 Undo Log 不需要写 Redo Log**

### 事务提交

入口函数: `trx_commit() --> trx_commit_low()`

在事务 commit 阶段，我们需要对 Undo Log 做一些处理.

对于 Insert Record 操作，我们可以直接清理 Undo Log, 因为 Insert 操作的记录只是对于本事务可见，所以它们不再需要被访问. 首先判断 Insert Record 操作产生的 Undo Log 是否可以被重用，并设置状态为`TRX_UNDO_CACHED`或者`TRX_UNDO_TO_FREE`. 能否被复用的逻辑是该 Undo Log 所使用的 Page 数量为 1，并且所占 Page 的空间不足 3/4 即可被重用。

### 事务回滚

事务在回滚后, 需要对修改过的 Record 做回滚处理, Record 的回滚逻辑是通过获取回滚段上 Undo Log Segment 的 Record 通过row_undo_ins()回滚 Insert 操作、row_undo_mod()回滚 Update 操作。

## 浅析MVCC工作原理

**undo log在事务开启之前就产生，当事务提交的时候，不会删除undo log，因为可能需要rollback操作，要执行回滚（rollback）操作时，从缓存中读取数据。InnoDB会将事务对应的日志保存在删除list中，后台通过purge线程进行回收处理。**

以一条sql执行update、select过程来浅析MVCC的工作原理：

执行update操作，事务A提交时候（事务还没提交），会将数据进行备份，备份到对应的undo buffer，undo log保存了未提交之前的操作日志，User表数据肯定就是持久保存到InnoDB的数据文件IBD，默认情况。

这时事务B进行查询操作，是直接读undo buffer缓存的，这时事务A还没提交事务，要回滚（rollback），是不读磁盘的，先直接从undo buffer缓存读取。

![浅析MVCC工作原理](http://mkstatic.lianbian.net/202110112132609.png)

## 总结

写这篇文章的时候，开始只是准备简单的写一写，但是发现看到一些好的文章资料，还是想着自己去慢慢的把它啃下来，通过自己的理解把图画出来，希望我们一起进步吧，有什么问题，可以加我微信或者留言讨论。

本篇文章使用的源码是```MySQL8.0.13```，MySQL是C++编写的，如果有兴趣的，可以直接关注「连边」订阅号，回复```mysql8013```获取源码包。

我是直接使用`IntelliJ IDEA`编辑器，直接解压打开源码的，然后根据文中的关键字搜索文件代码即可。

### 巨人的肩膀

https://www.leviathan.vip/2019/02/14/InnoDB%E7%9A%84%E4%BA%8B%E5%8A%A1%E5%88%86%E6%9E%90-Undo-Log/



衷心感谢每一位认真读文章的人，我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。