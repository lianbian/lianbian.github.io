---
title: Redis是什么？
keywords: redis概念
categories: redis
summary: Redis（Remote Dictionary Server )，英文直译为远程字典服务。
author: 连边
date: 2022-04-29
tags:
  - redis

---

![redis是什么](https://mkstatic.lianbian.net/202204290656695.png)



Redis是什么？

Redis（Remote Dictionary Server )，英文直译为远程字典服务，我们从4个方面来剖析：

1. 支持网络
2. 基于内存亦可持久化的日志型数据库
3. Key Value数据库
4. 提供多种语言的API

以下我们来一一讲解。

支持网络，这点好理解，可以单独部署在网络上，实现应用程序对它的操作访问。

![支持网络](https://mkstatic.lianbian.net/202204290628498.png)

基于内存亦可持久化的日志型数据库，这点就是说Redis的读写是基于内存的，这也是它的性能高的一个原因，但我们都知道，内存中的数据会随着服务器的重启而丢失，

![什么是Redis-内存](https://mkstatic.lianbian.net/202204290638360.png)

![什么是Redis-内存重启](https://mkstatic.lianbian.net/202204290639517.png)

而怎么样保证数据不丢失呢，对，就是持久化到磁盘上，以便 Redis 重启时能够从磁盘中恢复原有的数据，这个过程就叫做Redis持久化，这也是概念中所说的可基于内存亦可持久化的日志型数据库。

![什么是Redis-写入到磁盘](https://mkstatic.lianbian.net/202204290638636.png)

这里深入讲解下持久化RDB和AOF。

RDB方式的持久化是通过快照（snapshotting）完成的，当符合一定条件时，Redis会自动将内存中所有的数据生成一份副本并存储在硬盘中，这个过程被称为“快照”。“快照”，就类似于拍照，摁下快门那一刻，所定格的照片，就称为“快照”。但是RDB方式实现持久化，一旦Redis异常退出，就会丢失最后一次快照之后更改的所有数据。为了降低因进程中止导致的数据丢失风险，可以使用AOF方式实现数据持久化。AOF持久化是以日志追加的形式记录服务器所处理的每一个新增、删除操作，查询操作不记录，以文本的方式记录，文件中可以看到详细的操作记录。

简单对比下RDB与AOF的优缺点。

**RDB的优缺点：**

优点：RDB持久化文件，速度比较快，而且存储的是一个二进制文件，传输起来很方便。

缺点：RDB无法保证数据的绝对安全，有时候就是1s也会有很大的数据丢失。

**AOF的优缺点：**

优点：AOF相对RDB更加安全，一般不会有数据的丢失或者很少，官方推荐同时开启AOF和RDB。

缺点：AOF持久化的速度，相对于RDB较慢，存储的是一个文本文件，到了后期文件会比较大，传输困难。

接下来我们来继续理解Key Value数据库，常见的key value数据库有memcache、redis；而在Java语言里边有一个常用的数据类型Map也是典型的key value，简而言之就是一个key，对应着一个值；它的查询复杂度为O(1)；这里也可以和哈希关联起来，若关键字为key，则其值存放在f(key)的存储位置上。由此，不需比较便可直接取得所查记录。称这个对应关系f为**哈希函数**，按这个思想建立的表为**哈希表**。

![什么是Redis-key-value](https://mkstatic.lianbian.net/202204290639032.png)

最后一点，提供多种语言的API，就是官方提供了很编程语言的驱动，能够方便的各种应用语言通过驱动来操作Redis API。

![什么是Redis-驱动](https://mkstatic.lianbian.net/202204290637581.png)



谢谢收看本期内容，我们下期再见。

我是连边，简化你的求知路径。

欢迎关注连边，不错过精彩文章。

![订阅号@连边](https://mkstatic.lianbian.net/202204290653427.jpg)