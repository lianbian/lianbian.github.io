---
title: LinkedList源码解析
date: 2021-08-12
categories: java
tags: java
---



大家好，我是连边。

今天七夕情人节，在这里祝天下有情人终成眷属。



## 且以深情共白首

让自己的另一半了解自己的职业，还是挺重要的。

朋友眼中的我：修电脑的；

爸妈眼中的我：熬夜加班的栋梁材；

岳父岳母眼中的我：没有铁饭碗的不靠谱青年。

亲身经历证明，这些都不重要，身为一名神秘而复杂的程序员，你只需要得到你最在乎的那个人的理解和认可就行了。

靠什么？必须是才华和深情。

狗子们，我们一起来用链表定制一辆幸福号地铁送给心目中的那个她吧，奔赴星辰大海，白首不离。



## 地铁站链表

我们先以长沙地铁一号线的地铁站名来分析链表的制作过程。

![长沙地铁一号线](http://mkstatic.lianbian.net/20210814154739.jpeg)

**“培元桥到了，下一站：五一广场，请前往五一广场的乘客做好下车准备。”**



培元桥站的图示：

![image-20210814155418743](http://mkstatic.lianbian.net/20210814155418.png)

```抽象```一下的站点示意图：

<img src="http://mkstatic.lianbian.net/20210814095804.png" alt="image-20210814095804012"/>

用代码来定义这个车站：

```java
class Station<E> {
    // 车站本身
    E item;
    // 下一站
    Node<E> next;

    public Station(E item, Node<E> next) {
      this.item = item;
      this.next = next;
    }
}
```

根据这个上边讲解，我们不难总结出其特点：**站点不仅包含了自身的信息，还包含了下一站信息。**

通过上边的车站节点，我们画出其他站点示意图（画出4个，不全部画出。）

<img src="http://mkstatic.lianbian.net/20210814101424.png" alt="image-20210814101424586"  />

如果要让车站信息更实用，使之富有动态感，对乘客的提示性更强，我们就需要把它们链接起来，成为实用的数据结构，像一号线交通站点图那样。

![站点链表](http://mkstatic.lianbian.net/20210814111856.gif)



### 定义地铁链表

```java
class StationList<E> {
    // 站点总数
    int size;
    // 第一站
    Station<E> first;
    // 最后一站
    Station<E> last;

    // 车站，就是上边抽象的那个车站结构
    static class Station<E> {
        // 本车站信息
        E item;
        // 下一站车站信息
        Station<E> next;

        public Station(E item, Station<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    public void add(E e) {
        final Station<E> l = last;
        final Station<E> newNode = new Station<>(e, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }

        size++;
    }

    public E get(int index) {
        // 循环从头开始找
        Station<E> x = first;
        for (int i = 0; i < index; i++) {
            x = x.next;
        }
        return x.item;
    }
}
```



## 定制幸福号地铁

每个人对幸福的定义都不一样，那定制的站名就不一样。说到我的深情，简洁而温暖，温暖而持久。

>“我随时都有空。”
>
>“娶到你像做梦一样。”
>
>“工资转给你了。”
>
>“崽睡了，我去买夜宵了。”
>
>“我去背面试题了。”

```java
public static void main(String[] args) {
    StationList<String> stationList = new StationList<>();
    // 初始化幸福号地铁链表
    stationList.add("我随时都有空。");
    stationList.add("娶到你像做梦一样。");
    stationList.add("工资转给你了。");
    stationList.add("崽睡了，我去买夜宵了。");
    stationList.add("我去背面试题了。");

    System.out.printf("「幸福号地铁」共有 %s 站", stationList.size);
    System.out.println();
    System.out.println("==========================");
    System.out.printf("「幸福号地铁」从「%s」开往「%s」", stationList.first.item, stationList.last.item);
    System.out.println();
    System.out.println("==========================");
    System.out.printf("「幸福号地铁」告诉我们，幸福%d要素：", stationList.size);
    System.out.println();
    for (int i = 0; i < stationList.size; i++) {
      System.out.println(stationList.get(i));
    }
}
```



## 琴瑟在御，莫不静好

每个靠谱的人都值得拥有自己的幸福，用你的才华和深情去定制属于你们的幸福号地铁吧。

今天就别加班了。

毕竟，陪伴才是最长情的告白。



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

如果您觉得我的文章有收获，可以分享给您的朋友或者朋友圈；

如果想实时的看到我的文章，您也可以点击下边的名片来关注我。