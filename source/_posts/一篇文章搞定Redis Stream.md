---
img: https://mkstatic.lianbian.net/202204010756622.png
title: 一篇文章搞定Redis Stream
keywords: redis stream
categories: redis
summary: Redis5.0推出的新类型Stream你了解吗？这一篇文章带你搞定Redis Stream的原理与实战。
author: 连边
date: 2022-04-04
tags:
  - redis
---



## 前言

大家好，我是连边。

今天给大家带来一篇关于`Redis Stream`的文章，文章会从消息队列的基本概念、然后基于Redis Stream基本操作，顺带着会讲Redis Stream的存储结构，最后配合在`Spring Boot`框架来讲解实战。阅读完这篇文章以后，基本上就能把Redis Stream拿捏住。

话不多说，开始～

也还是老规矩，先上导读图。

![搞定Redis Stream](https://mkstatic.lianbian.net/202204112322351.png)



## 概念

`Redis Stream` 是`Redis5.0`推出的一种专门用来处理`消息队列`场景的高级数据结构，是Redis下消息队列的最佳实现。

![stream全局图-不是我画的](https://mkstatic.lianbian.net/202204070735521.png)

这是一个很好的Redis Stream知识体系图，现在看着看不懂这个图也没有关系，我会由浅入深的给大家剖析该体系图。



## 什么是消息队列

`队列`是一种数据结构，对应到我们生活中的例子，就是排队。讲究着**先来先处理的原则**，目的是为了避免混乱与系统瘫痪。

**消息队列把我们具体的业务，都抽象成为“消息”。排队做核酸，也可以叫做“核酸”队列。**

刚说了队列是为了避免混乱与系统瘫痪，它还有一些其他的作用：

1. 消峰填谷

这点表现最明显的就是我们常说的秒杀系统，访问量在某一个刻，到达巅峰，然后又断崖式下跌。

根据具体的业务场景，把业务处理的压力压入到消息队列中，以时间轴为水平线，来处理业务。

![消峰填谷](https://mkstatic.lianbian.net/202204041252508.png)

2. 异步解耦

假如有一个电商系统，在商品下单后，需要通知客服小二核对地址，需要通知物流仓储系统发货，需要营销中心发放优惠券 等等一些业务。

![异步解耦](https://mkstatic.lianbian.net/202204041252375.png)

消息队列还有一些其他的作用，以上两个是比较典型的。

在`Reids`没有推出`Stream`数据类型的时候，我们也可以用`Redis`来做消息队列。分别是利用`Reids`的`list`和`订阅/发布`两种数据类型。

1. list

用push命令压入队列，用pop命令来拿出队列里边的消息。

```shell
## 在 lianbianKey 里边压入2个消息，messageContent1、messageContent2
lpush lianbianKey messageContent1
lpush lianbianKey messageContent2

## 在另外一端，lpop命令取出元素
## lpop移除并获取第一个元素
lpop lianbainKey
```

2. 订阅/发布

这个模式，和我们平常看电视频道类似，把电视台切换到湖南卫视，你就能自然看到当前电视台播放的节目（快乐大本营、天天向上 ...），就相当于你订阅了湖南卫视频道，在湖南卫视电视台发布节目的时候，你就能够很自然的接收到。但是这种模式有一个弊端，就是错过了就是错过了，错过了19:00的新闻联播，就是错过了。



都能实现消息队列，为什么`Redis`在5.0的版本里，还推出`Stream`数据类型呢？与`list`和`订阅/发布`又有什么异同呢？



## 为什么要Redis Stream

1. **标准化操作**

在没有`Redis Stream`推出的时候，我们知道利用`Reids`能够实现队列功能，但是大家实现得五花八门，没有一个统一的、标准的实现方式。最后导致的就是`Redis队列生态`显得乱，在推出Stream之后，后续实现队列都会使用该新的数据类型，利于标准化。

2. **取长补短**

不管是`list`还是`订阅/发布`模式，都有其弊端，比如list不能友好的重复消费，需要重复消费的话，需要程序代码去控制；而`订阅/发布模式`是一种转发消息的模式，只有订阅者在线的时候才能接收到消息，订阅者不在线期间产生的消息，就丢掉了。而Stream，有借鉴Kafka一些市面上成熟的消息队列的思想，可以消费失败重复消费，消息是持久化的。

3. **降低门槛**

不管是之前的`list`还是`订阅/发布`模式，其实就不能说是严谨的消息队列，它只能说是我们实现消息列队的基本数据结构，而`Stream`可以说是降低了我们在`Redis`中使用消息队列的门槛，封装了一些消费者常见的角色（生产者、主题、元素、消费者、消费组）。



## 消息队列的几个重要元素

从上面的讲解，我们把消息队列中的几个概念再着重讲一下，如果熟悉其他的消息队列的，还是很容易理解。

1. 生产者

生产消息的服务或者项目，比如上边例子中的**商品下单**。

2. 主题

主题就相当于我们电视台的频道，生产者负责把消息往主题里边塞，塞进消息之后，监听了该主题的消费者会从该主题里边拿出消息。在Redis Stream里边，主题名称就是我们的stream key键值。现在不懂也没关系，在后边直观体验环节能够直观感受到。

3. 元素

也可以叫做消息，就是我们生产者往主题里边塞的内容，都可以称之为**消息**、**元素**。

4. 消费者

监听主题拿出消息，对消息进行处理的，叫做消费者。如最上边例子中的拿订单号的仓库系统、物流系统、营销系统。

讲完了基本理论，我们用命令行的方式直观感受一下`Redis Stream`。

## 直观体验Redis Stream

**安装**

windows下，很多开发的时候，还是使用3.0+的版本redis（微软编译的安装包，但是一直没更新了。），但是`Stream`是Redis5.0的新特性，在github上找到了大牛的编译版本：

https://github.com/tporadowski/redis/releases

也可以关注我的公众号回复`redis5`获取百度网盘下载地址。

在安装好之后，就可以进行命令行的玩转了。

### 进入redis命令行

![redis命令行](https://mkstatic.lianbian.net/202204052136097.png)

### 创建主题并创建消息

创建`lianbianKey`主题，追加 `nickName LianBian`。

```shell
XADD key ID field string [field string ...]
```

![xadd追加和创建主题](https://mkstatic.lianbian.net/202204052143572.png)

可视化工具查看：

![可视化工具查看](https://mkstatic.lianbian.net/202204052146648.png)



### 独立消费

消费在`lianbianKey`主题中刚追加 `nickName LianBian`消息。

语法：

```shell
XREAD [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] ID [ID ...]
```

其中：

1. [COUNT count]，用于限定获取的消息数量；
2. [BLOCK milliseconds]，用于设置XREAD为阻塞模式，默认为非阻塞模式；
3. ID，用于设置由哪个消息ID开始读取。使用0表示从第一条消息开始。（本例中就是使用0）此处需要注意，消息队列ID是单调递增的，所以通过设置起点，可以向后读取。在阻塞模式中，可以使用$，表示最新的消息ID。（在非阻塞模式下$无意义）。

XRED读消息时分为阻塞和非阻塞模式，使用BLOCK选项可以表示阻塞模式，需要设置阻塞时长。非阻塞模式下，读取完毕（即使没有任何消息）立即返回，而在阻塞模式下，若读取不到内容，则阻塞等待。

```shell
xread block 10000 streams lianbianKey $
```

阻塞读取lianbianKey主题的消息，最长阻塞10s，读到内容之后，立即返回。

![阻塞消费](https://mkstatic.lianbian.net/202204052155202.png)

典型的队列就是 XADD 配合 XREAD Block 完成。XADD负责生成消息，XREAD负责消费消息。

### 消费组模式

上边讲解的是一个消费者的模式，也是独立消费模式；Redis Stream也支持消费组模式，这点是借鉴了kafka的思想。当我们的生产者生产的消息过快，单个消费者的消费处理速度不能满足业务，我们就可以考虑使用消费组模式，来加快消息的处理。

多个消费者配合协作来消费同一个消息队列，比如消息队列中有10条消息，三个消费者分别消费其中的某些消息，比如消费者A消费消息1、2、5、8，消费者B消费消息4、9、10，而消费者C消费消息3、6、7。

![文字对应的消费组模型-不是我画的](https://mkstatic.lianbian.net/202204052201508.jpg)



如果觉得上图有一些乱，可以看下边的结构图，来加深理解。



![消费组结构图-不是我画的](https://mkstatic.lianbian.net/202204070745630.png)

消费者组模式的支持主要由两个命令实现：

1. XGROUP，用于管理消费者组，提供创建组，销毁组，更新组起始消息ID等操作
2. XREADGROUP，分组消费消息操作

消费组演示：

1. 创建消费组

```shell
XGROUP create lianbianKey lianbianGroup 0
```

2. 创建消息

![创建消息](https://mkstatic.lianbian.net/202204052208819.png)

3. 消费组模式消费消息

```shell
XREADGROUP group lianbianGroup consumerA count 1 streams lianbianKey >
```

![消费组模式第一条消息](https://mkstatic.lianbian.net/202204052211840.png)

注意对比和压入时候的消息ID；

依次分配给`consumerB`、`consumerC`来消费：

![依次消费](https://mkstatic.lianbian.net/202204052215363.png)

**可以进行组内消费的基本原理是，STREAM类型会为每个组记录一个最后处理（交付）的消息ID（last_delivered_id），这样在组内消费时，就可以从这个值后面开始读取，保证不重复消费。**

讲完了独立消费、消费组消费，接下来继续讲**消费确认**。

### ACK（消息确认）

在消费消息之后，我们是要给消息队列一个反馈，Redis Stream是用的`xack`来完成这部分工作的。

![消费确认示意简图](https://mkstatic.lianbian.net/202204052242986.png)

前面我们消费了几条消息，但是我们并没有消费确认，我们借助`xpending`命令来查看下：

![未确认消息统计](https://mkstatic.lianbian.net/202204052244639.png)

表示有哪些消息没有确认，我们来操作下消息确认

![xack消费确认](https://mkstatic.lianbian.net/202204052249394.png)



在我们操作了ack命令之后，挂载在consumerA的数量减少了1（回顾最开始的那个体系图，挂载在消费组图中的pending_ids[]吗？就是记录的这个）。

基本操作讲完了，我们稍微总结一下。

xadd命令创建主题和消息，然后可以采取两种模式来消费消息，分别是**独立消费**和**消费组消费**，在消费消息之后，我们要使用ack来确认消费，至此，一条消息才算被处理完毕。

![stream全局图-不是我画的](https://mkstatic.lianbian.net/202204082236528.png)

现在再看这张图，是不是有很多东西已经理解了。

上边讲的都是消息的正常情况，但是在我们的实际场景中，难免有一些异常情况，接下来，我继续给大家讲解Redis Stream是怎么应对那些实际场景中的异常情况的。

### PEL（等待列表）

某个消费者，读取了某条消息之后进程就宕机了，这条消息可能会丢失，因为其他消费者不能再次消费到该消息了，这种情况下，Redis Stream是怎么样保证这条消息不丢失的呢？

看到数据丢失，我们第一个想到的应该是要想到备份。对，就是备份。

在消息读取的时候，同时会在pending列表中存入一个消息ID，当客户端重新连上之后，可以再次收到PEL中的消息ID列表。这也是我们`xpending`命令的基本底层原理。

xpending命令

![PEL详情](https://mkstatic.lianbian.net/202204082329393.png)

xpending命令有4个属性：

1. 消息ID
2. 所属消费者
3. IDLE，已读取时长
4. 消息被读取次数

有了这样一个Pending机制，就意味着在某个消费者读取消息但未处理后，消息是不会丢失的。

此时还有一个问题，就是若某个消费者宕机之后，没有办法再上线了，那么就需要将该消费者Pending的消息，转移给其他的消费者处理，就是消息转移。

### 消息转移

消息转移的操作时将某个消息转移到自己的Pending列表中。使用语法`XCLAIM`来实现，需要设置组、转移的目标消费者和消息ID，同时需要提供IDLE（已被读取时长），只有超过这个时长，才能被转移。演示如下：

![待转移的消息](https://mkstatic.lianbian.net/202204082338305.png)

```shell
XCLAIM lianbianKey lianbianGroup consumerB 3600000 1649166116013-0
```

![转移结果](https://mkstatic.lianbian.net/202204082339586.png)

以上代码，完成了一次消息转移。转移除了要指定ID外，还需要指定IDLE，保证是长时间未处理的才被转移。被转移的消息的IDLE会被重置，用以保证不会被重复转移，以为可能会出现将过期的消息同时转移给多个消费者的并发操作，设置了IDLE，则可以避免后面的转移不会成功，因为IDLE不满足条件。例如下面的连续两条转移，第二条不会成功。

![重复转移](https://mkstatic.lianbian.net/202204082340480.png)

上边的命令因为时间没到，重复转移失败，还是归属于消费者B。

这就是消息转移。至此我们使用了一个Pending消息的ID，所属消费者和IDLE的属性，还有一个属性就是消息被读取次数，该属性的作用由于统计消息被读取的次数，包括被转移也算。这个属性主要用在判定是否为错误数据上。

### 坏消息问题，Dead Letter，死信问题

正如上面所说，如果某个消息，不能被消费者处理，也就是不能被XACK，这是要长时间处于Pending列表中，即使被反复的转移给各个消费者也是如此。此时该消息的被读取次数就会累加（上一节的例子可以看到），当累加到某个我们预设的临界值时，我们就认为是坏消息（也叫死信，DeadLetter，无法投递的消息），由于有了判定条件，我们将坏消息处理掉即可，删除即可。删除一个消息，使用`XDEL`语法。（这里是说被读取次数是我们在做业务的时候判断是否需要删除的依据，而xdel就是stream的一个普通的命令，删除队列中的某个消息。）

![xdel](https://mkstatic.lianbian.net/202204082345023.png)

![在pending中还存在](https://mkstatic.lianbian.net/202204082348833.png)

是普通命令，就记得xack

![xack后，PEL中无数据](https://mkstatic.lianbian.net/202204082349405.png)

至此，Redis Stream的直观体验告一段落，大家是否熟悉了呢？

赶紧实践一番吧。

## SpringBoot案例实战

在实际编码之前， 我们稍微整理下思路。

首先我们需要有生产者和消费者两个角色。生产者不断产生消息，消费者不断处理消息。

生产者我们直接用命令行替代，编码实现实时消费的模块。

最后达到的效果就是，命令行往指定的Key插入消息，在代码的命令行输出能看到消费的消息。

**引入依赖**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>2.5.6</version>
</dependency>
```



**创建监听**

监听需要继承`StreamListener`类，重写`onMessage`方法

```java
@Component
public class RedisStreamConsumerListener implements StreamListener<String, MapRecord<String ,String, String>> {
    static final Logger logger = LoggerFactory.getLogger(RedisStreamConsumerListener.class);

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        logger.info("message:: id={}, body={}", message.getId(), message.getValue());
    }
}
```



**注册Bean**

```java
@Bean
    public StreamMessageListenerContainer streamMessageListenerContainer() {
        AtomicInteger index = new AtomicInteger(1);
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors, processors, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("async-stream-comsumer-" + index.getAndDecrement());
            thread.setDaemon(true);
            return thread;
        });

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder().batchSize(10).executor(executor).errorHandler(new ErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                t.printStackTrace();
            }
        }).pollTimeout(Duration.ZERO).serializer(new StringRedisSerializer()).build();
        StreamMessageListenerContainer streamMessageListenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, streamMessageListenerContainerOptions);
        streamMessageListenerContainer.receive(Consumer.from("lianbianGroup", "consumer-a"), StreamOffset.create("lianbianKey", ReadOffset.lastConsumed()), streamListener);
        streamMessageListenerContainer.start();
        return streamMessageListenerContainer;
    }
```



**启动看效果**

![命令行生产者](https://mkstatic.lianbian.net/202204112311256.png)

![控制台输出结果](https://mkstatic.lianbian.net/202204112311670.png)



## 源代码地址

完整代码放在github上了，需要的可以自取：

https://github.com/lianbian/ReidsStreamDemo



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章

![订阅号@连边](https://mkstatic.lianbian.net/202204112323509.jpg)



参看链接：

https://zhuanlan.zhihu.com/p/60501638

http://www.redis.cn/topics/streams-intro.html
