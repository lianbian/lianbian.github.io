---
img: https://mkstatic.lianbian.net/202204010756622.png
title: ClickHouse数据类型
keywords: clickhouse
categories: clickhouse
summary: 了解一下ClickHouse那些数据类型，注意要看英文文档。
author: 连边
date: 2022-04-01
tags:
  - clickhouse
---

大家好，我是连边。

今天我们继续来学习`ClickHouse`的数据类型，其实这块资料重要的资料还是看官网。

[官网：ClickHouse数据类型](https://clickhouse.com/docs/en/sql-reference/data-types/)

老规矩，还是先上阅读导读：

![ClickHouse数据类型](https://mkstatic.lianbian.net/202204010756622.png)

## 分类与语法规则

`ClickHouse`与`MySQL`语法是及其相似的，我们先来看一个创建表语句。

```sql
CREATE TABLE dt
(
    `id` UInt32
    `name` String,
)
ENGINE = TinyLog;
```

`id UInt32`中，id为字段名称，UInt32是我们的数据类型，就是语法规则，为了方便记忆，我结合语法规则和使用场景把数据类型分成了四种类型：

1. 基本数据类型

基本数据类型是指我们Int、String ...常见类型。

2. 函数数据类型

看如下创建语句：

```sql
CREATE TABLE lc_t
(
    `id` UInt16,
    `strings` LowCardinality(String)
)
ENGINE = MergeTree()
ORDER BY id
```

`strings LowCardinality(String)`的申明部分，带着`()`与我们的函数调用相似，所以我给他取名叫做`函数类型`。

3. 场景类型

IP地址、经纬度都有固定的存储格式，而且也很常用，很多数据库都为常见的场景做了单独的类型，我们把这一类，归为`场景类型`。

4. 其他

那些实在不好分类的几种数据类型，就把它放在`其他类型`里。



做好了分类，我们依次来学习各种数据类型。



## 基本数据类型

与我们变成语言一样，常见的这一些类型。

### Int & Boolean

`ClickHouse`数据库的`Int`类型区分有符号与无符号，命名也非常的简单粗暴，用简单直接的命名顶替了其他数据库的`Int`、`BigInt`之类的。

| 类型   | 取值范围                   |
| ------ | -------------------------- |
| Int8   | [-128 : 127]               |
| Int16  | [-32768 : 32767]           |
| Int32  | [-2147483648 : 2147483647] |
| ...    |                            |
| UInt8  | [0 : 255]                  |
| UInt16 | [0 : 65535]                |
| UInt32 | [0 : 4294967295]           |

U开头的表示无符号，同样的空间，能够存储更大的值。上面表格没有写全，最大可以到`Int256、UInt256`。

这里需要单独拎出来说一下的是，在我们的`ClickHouse`里边，没有单独的`Boolean`类型，可以用`Int8`的0和1来表示。

### Float & Decimal

官方是推荐我们使用`Int`存储，比如我们存金额的时候，会采取存`分`为单位，这样子就避免存小数。

但是业务开发的时候，难免会碰到需要保留小数的后多少位，比如我们熟悉的`圆周率`。

| 类型          | 取值范围                                   |
| ------------- | ------------------------------------------ |
| Float32       | -2147483648 - 2147483647                   |
| Float64       | -9223372036854775808 - 9223372036854775807 |
|               |                                            |
| Decimal32(S)  | ( -1 * 10^(9 - S), 1 * 10^(9 - S) )        |
| Decimal64(S)  | ( -1 * 10^(18 - S), 1 * 10^(18 - S) )      |
| Decimal128(S) | ( -1 * 10^(38 - S), 1 * 10^(38 - S) )      |
| Decimal256(S) | ( -1 * 10^(76 - S), 1 * 10^(76 - S) )      |



### String & FixedString & UUID

在`ClickHouse`中，没有`varchar`这种结构，就是简单直接的提供了`String`类型，如果我们知道值是固定的长度，就用`FixdString`类型，它表示定长，与我们语言中的数组一样，申明的时候就固定了长度。比如我们存储MD5用`FixedString(16)` ，存储SHA256用 `FixedString(32)` 。

但是他提供了一种特殊的类型，就是`UUID`，专门用来存储UUID的值。

> UUID：通用唯一标识符 (UUID) 是一个 16 字节的数字，用于标识记录。

`ClickHouse`里边，[generateUUIDv4](https://clickhouse.com/docs/en/sql-reference/functions/uuid-functions/)可以生成UUID



### Date & DateTime 

时间类型，Date与DateTime的区别在于格式不一样，Date里边细分Date和Date32，DateTime细分DateTime64，他们之间的区别在于能存储的范围不一样。

| 名称       | 格式                | 取值范围                                                     |
| ---------- | ------------------- | ------------------------------------------------------------ |
| Date       | 2022-04-01          | [1970-01-01, 2149-06-06]                                     |
| Date32     | 2022-04-01          | [1925-01-01, 2283-11-11]                                     |
| DateTime   | 2022-04-01 00:00:00 | [1970-01-01 00:00:00, 2106-02-07 06:28:15]                   |
| DateTime64 | 2022-04-01 00:00:00 | [1925-01-01 00:00:00, 2283-11-11 23:59:59.99999999] (注意: 最大值的精度是8) |

`DateTime64` 类型可以像存储其他数据列一样存储时区信息，时区会影响 `DateTime64` 类型的值如何以文本格式显示。



### Enum

这个枚举和我们编程语言中的枚举可以说是一一对应了，分为8bit的枚举和16bit的枚举。

8 位枚举。它最多可以包含在 [-128, 127] 范围内枚举的 256 个值。 

16 位枚举。它最多可以包含 [-32768, 32767] 范围内枚举的 65536 个值。

如果使用基于ClickHouse来实现数据字典，这将是一个值得深入研究的东西。

https://clickhouse.com/docs/en/sql-reference/data-types/enum/



还有很多类型，我把它简单的归类一下，就不一一详细说明了，可以自行看文档。



## 函数数据类型

### Nullable

### LowCardinality

### Array

### AggregateFunction

### Nested

### Tuple

### SimpleAggregateFunction



## 场景类型

### Map | Json？

### Geo 保存经纬度

### Domains 域名 IP体系

#### IPv4

#### IPv6



## 其他

### 未定义 Nothing

### 时间戳 Interval



官方文档：

https://clickhouse.com/docs/en/sql-reference/data-types/

https://help.aliyun.com/document_detail/146000.html



## 最后

我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202204010752600.jpg)
