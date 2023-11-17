---
img: http://mkstatic.lianbian.net/202110291439151.png
title: 手写Redis的LRU淘汰策略
keywords: redis
categories: redis
summary: 今天我们这篇文章的目的只有一个，那就是`搞懂LRU淘汰策略`以及`实现一个LRU算法`。
author: 连边
date: 2021-10-29
tags:
  - redis
---

大家好，我是连边。

今天我们这篇文章的目的只有一个，那就是`搞懂LRU淘汰策略`以及`实现一个LRU算法`。

文章会结合图解循序渐进的讲解，跟着我的思路慢慢来就能看懂，我们开始吧。



## 文章导读

![LRU](http://mkstatic.lianbian.net/202110291439151.png)



## Redis的淘汰策略

为什么要有淘汰策略呢？

**因为存储内存的空间是有限的，所以需要有淘汰的策略。**

Redis的清理内存淘汰策略有哪些呢？

![Redis内存淘汰策略](http://mkstatic.lianbian.net/202110291452127.png)



## LRU算法简介

LRU是`Least Recently Used`的缩写，即`最近最少使用`，是一种常见的页面置换算法。

我们手机的后台窗口（苹果手机双击Home的效果），他总是会把最近常用的窗口放在最前边，而最不常用的应用窗口，就排列在后边了，如果再加上只能放置N个应用窗口的限制，淘汰最不常用的最近最少用的应用窗口，那就是一个活生生的`LRU`。

![手机后台应用窗口](http://mkstatic.lianbian.net/202110290847476.png)



## 实现思想推导

![手机应用案例](http://mkstatic.lianbian.net/202110290919765.png)

从上边的示意图，我们可以分析出这么几个点：

1. 有序；
2. 如果应用开满3个了，要淘汰最不常用的应用，每次新访问应用，需要把数据插入队头（按照业务可以设定左右哪一边是队头）；
3. O(1)复杂度是我们查找数据的追求，我们什么结构能够实现快速的O(1)查找呢？

![推导图](http://mkstatic.lianbian.net/202110291228385.png)

**通过上边的推导，我们就能得出，`LRU`算法核心是`HashMap + DoubleLinkedList`。**

思想搞明白了，我们接下来编码实现。



## 巧用LinkedHashMap

我们查看Java的`LinkedHashMap`使用说明。

![LinkedHashMap使用说明](http://mkstatic.lianbian.net/202110291019095.png)

**翻译：这种Map结构很适合构建LRU缓存。**

继承`LinkedHashMap`实现`LRU`算法：

```java
public class LRUDemo<K, V> extends LinkedHashMap<K, V> {
    private int capacity;

    public LRUDemo(int capacity) {
        super(capacity, 0.75F, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > capacity;
    }

    public static void main(String[] args) {
        LRUDemo lruDemo = new LRUDemo(3);
        lruDemo.put(1, "a");
        lruDemo.put(2, "b");
        lruDemo.put(3, "c");
        System.out.println(lruDemo.keySet());

        lruDemo.put(4, "d");
        lruDemo.put(5, "e");
        System.out.println(lruDemo.keySet());
    }
}
```

**重点讲解：**

1. 构造方法：`super(capacity, 0.75F, true)`，主要看第三个参数：

   1. ![order参数](http://mkstatic.lianbian.net/202110291025402.png)

   2. `true -> access-order // false -> insertion-order`即按照访问时间排序，还是按照插入的时间来排序

      ```java
      // 构造方法改成false
      super(capacity, 0.75F, false);
      // 使用示例
      public static void main(String[] args) {
        LRUDemo lruDemo = new LRUDemo(3);
        lruDemo.put(1, "a");
        lruDemo.put(2, "b");
        lruDemo.put(3, "c");
        System.out.println(lruDemo.keySet());
      
        lruDemo.put(1, "y");
        // 构造方法order=true，输出：[2,3,1]，
        // 构造方法order=false，输出：[1,2,3]，
        System.out.println(lruDemo.keySet());
      }
      ```

      

2. `removeEldestEntry`方法：什么时候移除最年长的元素。



通过上面，相信大家对`LRU`算法有所理解了，接下来我们不依赖JDK的`LinkedHashMap`，通过我们自己的理解，动手实现一个`LRU`算法，让我们的`LRU`算法刻入我们的大脑。



## 手写LRU

上边的推导图中可以看出，我们用`HashMap`来做具体的数据储存，但是我们还需要构造一个`DoubleLinkedList`对象（结构体）来储存`HashMap`的具体`key`顺序关系。

### 第一步：构建DoubleLinkedList对象

1. 所以我们现在**第一步**，就是构建一个`DoubleLinkedList`对象：

![DoubleLinkedList示意图](http://mkstatic.lianbian.net/202110291249801.png)

我们可以从`HashMap`源码中找一些灵感，他们都是使用一个`Node`静态内部类来储存节点的值。

### 第二步：构建节点

通过上边的示意图，我们可以得知**节点**应该要储存的内容：

1. key
2. value
3. prev节点
4. next节点

翻译成代码：

```java
class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    public Node() {
        this.prev = this.next = null;
    }

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
        this.prev = this.next = null;
    }
}
```

### 第三步：初始化DoubleLinkedList对象

![DoubleLinkedList初始化示意图](http://mkstatic.lianbian.net/202110291312430.png)

还是通过上边的示意图，我们可以得知**DoubleLinkedList对象**应该要储存的内容：

1. 头节点
2. 尾节点

翻译成代码：

```java
class DoubleLinkedList<K, V> {
    Node<K, V> head;
    Node<K, V> tail;

    // 构造方法
    public DoubleLinkedList(){
        head = new Node<>();
        tail = new Node<>();
        head.next = tail;
        tail.prev = head;
    }
}
```

#### 从头添加节点

![从头添加节点](http://mkstatic.lianbian.net/202110291329584.png)

翻译成代码：

```java
public void addHead(Node<K, V> node) {
    node.next = head.next;
    node.prev = head;
    head.next.prev = node;
    head.next = node;
}
```

#### 删除节点

![删除节点](http://mkstatic.lianbian.net/202110291341404.png)

翻译成代码：

```java
public void removeNode(Node<K, V> node) {
    node.next.prev = node.prev;
    node.prev.next = node.next;
    node.prev = null;
    node.next = null;
}
```

#### 获取最后一个节点

```java
public Node getLast() {
    return tail.prev;
}
```

### 第四步：LRU对象属性

#### cacheSize

```java
private int cacheSize;
```

#### map

```java
Map<Integer, Node<Integer, String>> map;
```

#### doubleLinkedList

```java
DoubleLinkedList<Integer, String> doubleLinkedList;
```

### 第五步：LRU对象的方法

#### 构造方法

```java
public LRUDemo(int cacheSize) {
    this.cacheSize = cacheSize;
    map = new HashMap<>();
    doubleLinkedList = new DoubleLinkedList<>();
}
```

#### refreshNode刷新节点

```java
public void refreshNode(Node node) {
    doubleLinkedList.removeNode(node);
    doubleLinkedList.addHead(node);
}
```

#### get节点

```java
public String get(int key) {
    if (!map.containsKey(key)) {
        return "";
    }

    Node<Integer, String> node = map.get(key);
    refreshNode(node);
    return node.value;
}
```

#### put节点

```java
public void put(int key, String value) {
    if (map.containsKey(key)) {
        Node<Integer, String> node = map.get(key);
        node.value = value;
        map.put(key, node);

        refreshNode(node);
    } else {
        if (map.size() == cacheSize) {
            Node lastNode = doubleLinkedList.getLast();
            map.remove(lastNode.key);
            doubleLinkedList.removeNode(lastNode);
        }

        Node<Integer, String> newNode = new Node<>(key, value);
        map.put(key, newNode);
        doubleLinkedList.addHead(newNode);
    }
}
```

### 第六步：测试

```java
public static void main(String[] args) {
    LRUDemo lruDemo = new LRUDemo(3);
    lruDemo.put(1, "美团");
    lruDemo.put(2, "微信");
    lruDemo.put(3, "抖音");
    lruDemo.put(4, "微博");
    System.out.println(lruDemo.map.keySet());

    System.out.println(lruDemo.get(2));
}
```



## 总结

`LRU`算法到这里就写完啦，[完整的代码](https://www.lianbian.net/2021/10/29/Redis/LRU%E5%AE%8C%E6%95%B4%E4%BB%A3%E7%A0%81/)可以从阅读原文的链接地址获取。

希望看完这篇文章之后，彻底弄懂LRU算法。

衷心感谢每一位认真读文章的人，我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

参考资料：

https://www.bilibili.com/video/BV1Hy4y1B78T?p=64



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

关注 `订阅号@连边` 不错过精彩文章。

![订阅号@连边](https://mkstatic.lianbian.net/202203160758473.jpg)