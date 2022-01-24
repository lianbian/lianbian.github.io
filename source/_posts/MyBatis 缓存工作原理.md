## 荐言

大家好，我是连边。

看这篇文章之间，要上厕所的去上厕所，准备好瓜子与板凳。

因为文章是那种看了停不下来，但是又确实很长的文章系列。

面试的时候，就会直接问你：“了解MyBatis的缓存工作原理吗？”

如果不了解，那么你赶快把这篇文章阅读下来，可以分分钟虐哭面试官，就怕面试官不问。



文章作者：双子孤狼

原文链接：https://blog.csdn.net/zwx900102/article/details/108696005



## 前言

在计算机的世界中，缓存无处不在，操作系统有操作系统的缓存，数据库也会有数据库的缓存，各种中间件如Redis也是用来充当缓存的作用，编程语言中又可以利用内存来作为缓存。自然的，作为一款优秀的ORM框架，MyBatis中又岂能少得了缓存，那么本文的目的就是带领大家一起探究一下MyBatis的缓存是如何实现的。给我五分钟，带你彻底掌握MyBatis的缓存工作原理

## 为什么要缓存

在计算机的世界中，CPU的处理速度可谓是一马当先，远远甩开了其他操作，尤其是I/O操作，除了那种CPU密集型的系统，其余大部分的业务系统性能瓶颈最后或多或少都会出现在I/O操作上，所以为了减少磁盘的I/O次数，那么缓存是必不可少的，通过缓存的使用我们可以大大减少I/O操作次数，从而在一定程度上弥补了I/O操作和CPU处理速度之间的鸿沟。而在我们ORM框架中引入缓存的目的就是为了减少读取数据库的次数，从而提升查询的效率。

## MyBatis缓存

MyBatis中的缓存相关类都在cache包下面，而且定义了一个顶级接口Cache，默认只有一个实现类PerpetualCache，PerpetualCache中是内部维护了一个HashMap来实现缓存。

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210006619.png)

下图就是MyBatis中缓存相关类：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210006957.png)

需要注意的是decorators包下面的所有类也实现了Cache接口，那么为什么我还是要说Cache只有一个实现类呢？其实看名字就知道了，这个包里面全部是装饰器，也就是说这其实是装饰器模式的一种实现。

我们随意打开一个装饰器：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210006080.png)

可以看到，最终都是调用了delegate来实现，只是将部分功能做了增强，**其本身都需要依赖Cache的唯一实现类PerpetualCache(因为装饰器内需要传入Cache对象，故而只能传入PerpetualCache对象，因为接口是无法直接new出来传进去的)。**

在MyBatis中存在两种缓存，即**一级缓存**和**二级缓存**。

## 一级缓存

一级缓存也叫本地缓存，在MyBatis中，一级缓存是在会话(SqlSession)层面实现的，这就说明一级缓存作用范围只能在同一个SqlSession中，跨SqlSession是无效的。

MyBatis中一级缓存是默认开启的，不需要任何配置。
我们先来看一个例子验证一下一级缓存是不是真的存在，作用范围又是不是真的只是对同一个SqlSession有效。

### 一级缓存真的存在吗

```java
package com.lonelyWolf.mybatis;

import com.lonelyWolf.mybatis.mapper.UserAddressMapper;
import com.lonelyWolf.mybatis.mapper.UserMapper;
import com.lonelyWolf.mybatis.model.LwUser;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestMyBatisCache {
    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        //读取mybatis-config配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        //创建SqlSessionFactory对象
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //创建SqlSession对象
        SqlSession session = sqlSessionFactory.openSession();

        UserMapper userMapper = session.getMapper(UserMapper.class);
        List<LwUser> userList =  userMapper.selectUserAndJob();
        List<LwUser> userList2 =  userMapper.selectUserAndJob();
    }
}
```

执行后，输出结果如下：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210007347.png)

我们可以看到，sql语句只打印了一次，这就说明第2次用到了缓存，这也足以证明一级缓存确实是存在的而且默认就是是开启的。

### 一级缓存作用范围

现在我们再来验证一下一级缓存是否真的只对同一个SqlSession有效，我们对上面的示例代码进行如下改变：

 ```java
  SqlSession session1 = sqlSessionFactory.openSession();
  SqlSession session2 = sqlSessionFactory.openSession();
 
  UserMapper userMapper1 = session1.getMapper(UserMapper.class);
  UserMapper userMapper2 = session2.getMapper(UserMapper.class);
  List<LwUser> userList =  userMapper1.selectUserAndJob();
  List<LwUser> userList2 =  userMapper2.selectUserAndJob();
 ```

这时候再次运行，输出结果如下：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210008664.png)

可以看到，打印了2次，没有用到缓存，也就是不同SqlSession中不能共享一级缓存。

### 一级缓存原理分析

首先让我们来想一想，既然一级缓存的作用域只对同一个SqlSession有效，那么一级缓存应该存储在哪里比较合适是呢？

是的，自然是存储在SqlSession内是最合适的，那我们来看看SqlSession的唯一实现类DefaultSqlSession：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210008409.png)

DefaultSqlSession中只有5个成员属性，后面3个不用说，肯定不可能用来存储缓存，然后Configuration又是一个全局的配置文件，也不合适存储一级缓存，这么看来就只有Executor比较合适了，因为我们知道，SqlSession只提供对外接口，实际执行sql的就是Executor。

既然这样，那我们就进去看看Executor的实现类BaseExecutor：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210008935.png)

看到果然有一个localCache。而上面我们有提到PerpetualCache内缓存是用一个HashMap来存储缓存的，那么接下来大家肯定就有以下问题：

缓存是什么时候创建的？
缓存的key是怎么定义的？
缓存在何时使用
缓存在什么时候会失效?
接下来就让我们逐一分析

**一级缓存CacheKey的构成**

既然缓存那么肯定是针对的查询语句，一级缓存的创建就是在BaseExecutor中的query方法内创建的：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210009975.png)

createCacheKey这个方法的代码就不贴了，在这里我总结了一下CacheKey的组成，CacheKey主要是由以下6部分组成

1. 将Statement中的id添加到CacheKey对象中的updateList属性

2. 将offset(分页偏移量)添加到CacheKey对象中的updateList属性(如果没有分页则默认0)

3. 将limit(每页显示的条数)添加到CacheKey对象中的updateList属性(如果没有分页则默认Integer.MAX_VALUE)

4. 将sql语句(包括占位符?)添加到CacheKey对象中的updateList属性

5. 循环用户传入的参数，并将每个参数添加到CacheKey对象中的updateList属性

6. 如果有配置Environment，则将Environment中的id添加到CacheKey对象中的updateList属性

   

   **一级缓存的使用**

   创建完CacheKey之后，我们继续进入query方法：

   ![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210010064.png)

可以看到，在查询之前就会去localCache中根据CacheKey对象来获取缓存，获取不到才会调用后面的queryFromDatabase方法

**一级缓存的创建**
queryFromDatabase方法中会将查询得到的结果存储到localCache中

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210011515.png)

**一级缓存什么时候会被清除**
一级缓存的清除主要有以下两个地方：

1. 就是获取缓存之前会先进行判断用户是否配置了flushCache=true属性（参考一级缓存的创建代码截图），如果配置了则会清除一级缓存。
2. MyBatis全局配置属性localCacheScope配置为Statement时，那么完成一次查询就会清除缓存。
3. 在执行commit，rollback，update方法时会清空一级缓存。
4. PS：利用插件我们也可以自己去将缓存清除，后面我们会介绍插件相关知识。

## 二级缓存

一级缓存因为只能在同一个SqlSession中共享，所以会存在一个问题，在分布式或者多线程的环境下，不同会话之间对于相同的数据可能会产生不同的结果，因为跨会话修改了数据是不能互相感知的，所以就有可能存在脏数据的问题，正因为一级缓存存在这种不足，所以我们需要一种作用域更大的缓存，这就是二级缓存。

### 二级缓存的作用范围

一级缓存作用域是SqlSession级别，所以它存储的SqlSession中的BaseExecutor之中，但是二级缓存目的就是要实现作用范围更广，那肯定是要实现跨会话共享的，在MyBatis中二级缓存的作用域是namespace，也就是作用范围是同一个命名空间，所以很显然二级缓存是需要存储在SqlSession之外的，那么二级缓存应该存储在哪里合适呢？

在MyBatis中为了实现二级缓存，专门用了一个装饰器来维护，这就是我们上一篇文章介绍Executor时还留下的没有介绍的一个对象：CachingExecutor。

### 如何开启二级缓存

二级缓存相关的配置有三个地方：
1、mybatis-config中有一个全局配置属性，这个不配置也行，因为默认就是true。

```java
<setting name="cacheEnabled" value="true"/>
```

2、在Mapper映射文件内需要配置缓存标签：

```java
<cache/>
或
<cache-ref namespace="com.lonelyWolf.mybatis.mapper.UserAddressMapper"/>
```

3、在select查询语句标签上配置useCache属性，如下：

```java
<select id="selectUserAndJob" resultMap="JobResultMap2" useCache="true">
        select * from lw_user
    </select>
```


以上配置第1点是默认开启的，也就是说我们只要配置第2点就可以打开二级缓存了，而第3点是当我们需要针对某一条语句来配置二级缓存时候则可以使用。

不过开启二级缓存的时候有两点需要注意：
1、需要commit事务之后才会生效
2、如果使用的是默认缓存，那么结果集对象需要实现序列化接口(Serializable)

如果不实现序列化接口则会报如下错误：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210013166.png)

接下来我们通过一个例子来验证一下二级缓存的存在，还是用上面一级缓存的例子进行如下改造：

```java
SqlSession session1 = sqlSessionFactory.openSession();
UserMapper userMapper1 = session1.getMapper(UserMapper.class);
List<LwUser> userList =  userMapper1.selectUserAndJob();
session1.commit();//注意这里需要commit,否则缓存不会生效

SqlSession session2 = sqlSessionFactory.openSession();
UserMapper userMapper2 = session2.getMapper(UserMapper.class);
List<LwUser> userList2 =  userMapper2.selectUserAndJob();
```


然后UserMapper.xml映射文件中，新增如下配置：

```java
<cache/>
```


运行代码，输出如下结果:

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210014486.png)

上面输出结果中只输出了一次sql，说明用到了缓存，而因为我们是跨会话的，所以肯定就是二级缓存生效了。

### 二级缓存原理分析

上面我们提到二级缓存是通过CachingExecutor对象来实现的，那么就让我们先来看看这个对象：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210015864.png)

我们看到CachingExecutor中只有2个属性，第1个属性不用说了，因为CachingExecutor本身就是Executor的包装器，所以属性TransactionalCacheManager肯定就是用来管理二级缓存的，我们再进去看看TransactionalCacheManager对象是如何管理缓存的：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210015449.png)

TransactionalCacheManager内部非常简单，也是维护了一个HashMap来存储缓存。
HashMap中的value是一个TransactionalCache对象，继承了Cache。

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210015136.png)

注意上面有一个属性是临时存储二级缓存的，为什么要有这个属性，我们下面会解释。

### 二级缓存的创建和使用

我们在读取mybatis-config全局配置文件的时候会根据我们配置的Executor类型来创建对应的三种Executor中的一种，然后如果我们开启了二级缓存之后，只要开启(全局配置文件中配置为true)就会使用CachingExecutor来对我们的三种基本Executor进行包装，即使Mapper.xml映射文件没有开启也会进行包装。

接下来我们看看CachingExecutor中的query方法：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210015748.png)

上面方法大致经过如下流程：

1. 创建一级缓存的CacheKey

2. 获取二级缓存

3. 如果没有获取到二级缓存则执行被包装的Executor对象中的query方法，此时会走一级缓存中的流程。

4. 查询到结果之后将结果进行缓存。

   

   需要注意的是在事务提交之前，并不会真正存储到二级缓存，而是先存储到一个临时属性，等事务提交之后才会真正存储到二级缓存。这么做的目的就是防止脏读。因为假如你在一个事务中修改了数据，然后去查询，这时候直接缓存了，那么假如事务回滚了呢？所以这里会先临时存储一下。
   所以我们看一下commit方法：

   ![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210016985.png)

### 二级缓存如何进行包装

最开始我们提到了一些缓存的包装类，这些都到底有什么用呢？
在回答这个问题之前，我们先断点一下看看获取到的二级缓存长啥样：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210016890.png)

从上面可以看到，经过了层层包装，从内到外一次经过如下包装：

1. PerpetualCache：第一层缓存，这个是缓存的唯一实现类，肯定需要。
2. LruCache：二级缓存淘汰机制之一。因为我们配置的默认机制，而默认就是LRU算法淘汰机制。淘汰机制总共有4中，我们可以自己进行手动配置。
3. SerializedCache：序列化缓存。这就是为什么开启了默认二级缓存我们的结果集对象需要实现序列化接口。
4. LoggingCache：日志缓存。
5. SynchronizedCache：同步缓存机制。这个是为了保证多线程机制下的线程安全性。
   下面就是MyBatis中所有缓存的包装汇总：

缓存包装器	描述	作用	装饰条件
PerpetualCache	缓存默认实现类	-	基本功能，默认携带
LruCache	LRU淘汰策略缓存（默认淘汰策略）	当缓存达到上限，删除最近最少使用缓存	eviction=“LRU”
FifoCache	FIFO淘汰策略缓存	当缓存达到上限，删除最先入队的缓存	eviction=“FIFO”
SoftCache	JVM软引用淘汰策略缓存	基于JVM的SoftReference对象	eviction=“SOFT”
WeakCache	JVM弱引用淘汰策略缓存	基于JVM的WeakReference对象	eviction=“WEAK”
LoggingCache	带日志功能缓存	输出缓存相关日志信息	基本功能，默认包装
SynchronizedCache	同步缓存	基于synchronized关键字实现，用来解决并发问题	基本功能，默认包装
BlockingCache	阻塞缓存	get/put操作时会加锁，防止并发，基于Java重入锁实现	blocking=true
SerializedCache	支持序列化的缓存	通过序列化和反序列化来存储和读取缓存	readOnly=false(默认)
ScheduledCache	定时调度缓存	操作缓存时如果缓存已经达到了设置的最长缓存时间时会移除缓存	flushInterval属性不为空
TransactionalCache	事务缓存	在TransactionalCacheManager中用于维护缓存map的value值	-

## 二级缓存应该开启吗

既然一级缓存默认是开启的，而二级缓存是需要我们手动开启的，那么我们什么时候应该开启二级缓存呢？

1、因为所有的update操作(insert,delete,uptede)都会触发缓存的刷新，从而导致二级缓存失效，所以二级缓存适合在读多写少的场景中开启。

2、因为二级缓存针对的是同一个namespace，所以建议是在单表操作的Mapper中使用，或者是在相关表的Mapper文件中共享同一个缓存。

## 自定义缓存

一级缓存可能存在脏读情况，那么二级缓存是否也可能存在呢？

是的，默认的二级缓存毕竟也是存储在本地缓存，所以对于微服务下是可能出现脏读的情况的，所以这时候我们可能会需要自定义缓存，比如利用redis来存储缓存，而不是存储在本地内存当中。

### MyBatis官方提供的第三方缓存

MyBatis官方也提供了一些第三方缓存的支持，如：encache和redis。下面我们以redis为例来演示一下：
引入pom文件：

```java
<dependency>
    <groupId>org.mybatis.caches</groupId>
    <artifactId>mybatis-redis</artifactId>
    <version>1.0.0-beta2</version>
  </dependency>
```

然后缓存配置如下：

```java
<cache type="org.mybatis.caches.redis.RedisCache"></cache>
```

然后在默认的resource路径下新建一个redis.properties文件：

```java
host=localhost
port=6379
```

然后执行上面的示例，查看Cache，已经被Redis包装：

![图片太多了，连边不想写图片描述了](http://mkstatic.lianbian.net/202201210018350.png)

## 自己实现二级缓存

如果要实现一个自己的缓存的话，那么我们只需要新建一个类实现Cache接口就好了，然后重写其中的方法,如下：

```java
package com.lonelyWolf.mybatis.cache;

import org.apache.ibatis.cache.Cache;

public class MyCache implements Cache {
    @Override
    public String getId() {
        return null;
    }
    @Override
    public void putObject(Object o, Object o1) {
        
    }
    @Override
    public Object getObject(Object o) {
        return null;
    }

    @Override
    public Object removeObject(Object o) {
        return null;
    }

    @Override
    public void clear() {
    }

    @Override
    public int getSize() {
        return 0;
    }
}
```



上面自定义的缓存中,我们只需要在对应方法，如putObject方法，我们把缓存存到我们想存的地方就行了，方法全部重写之后，然后配置的时候type配上我们自己的类就可以实现了，在这里我们就不做演示了

## 总结

本文主要分析了MyBatis的缓存是如何实现的，并且分别演示了一级缓存和二级缓存，并分析了一级缓存和二级缓存所存在的问题，最后也介绍了如何使用第三方缓存和如何自定义我们自己的缓存，通过本文，我想大家应该可以彻底掌握MyBatis的缓存工作原理了。

