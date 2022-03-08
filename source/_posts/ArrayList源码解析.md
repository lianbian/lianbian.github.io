---
img: http://mkstatic.lianbian.net/Java%E9%9B%86%E5%90%88.png
title: ArrayList源码解析
keywords: java arraylist
categories: java
summary: 动态图来图解ArrayList源码，图解分析原理，实战面试题检测，阅读JDK源码进一步巩固理解。
author: 连边
date: 2021-08-09
tags:
  - java
---



大家好，我是连边。

## 前言

开始写```Java集合```图解源码系列的文章，本篇是开篇，主角是```ArrayList```。

这个系列按照这个步骤来讲解：

**图解分析原理，实战面试题检测，阅读JDK源码进一步巩固理解。**

阅读本系列文章，不需要数据结构的基础，带上认真的态度完全可以吃透。

## Java集合的整体认识

java集合框架提供了一套性能优良，使用方便的接口和类，它们位于```java.util```包中。

![Java集合](http://mkstatic.lianbian.net/Java%E9%9B%86%E5%90%88.png)

今天这篇文章的主角是```ArrayList```，从上图可以看出，```ArrayList```实现List接口，和它同根同级的还有```LinkedList```，```ArrayList```同时还实现了RandomAccess, Cloneable, java.io.Serializable，我们利用类图来直观说明。

![ArrayList类图](http://mkstatic.lianbian.net/ArrayList-20210811144456212.png)

## 图解基本原理

### 数组与动态数组（ArrayList）

数组的索引从0开始；

Java语言中提供的数组是用来储存固定大小的同类型元素；

Java中可以使用两种方式来声明数组：

```java
// 申明
String[] arrayRefVar;
String arrayRefVar[];
```

Java中数组的创建方式同样有两种：

```java
// 创建
int arraySize = 6;
String[] arrayRefVar = new String[arraySize];
// 创建并且初始化
String[] arrayRefVar = {"A", "B", "C"};
```



`ArrayList`是一种以数组实现的`List`，与数组相比，具有动态扩展的能力，因此也可称之为```动态数组```。

### 增删改查和扩容

#### 添加元素

1. 末尾添加元素

   **场景：** 存在长度为6的数组，存在A、B、C三个值，把D值（索引为3）添加到数组末尾；

   **步骤：** 直接进行压入操作就可以完成操作，然后再挪动size；

![末尾添加元素](http://mkstatic.lianbian.net/arrayList6.gif)

2. 中间添加元素

   **场景：** 存在长度为6的数组，存在A、B、C三个值，把D值（索引为2）添加到AB之间；

   **步骤：** 首先需要把添加位置之后的元素往后挪，挪动完成之后，把指定的元素插入到挪出来的空位，然后挪动size；

   

![中间添加元素](http://mkstatic.lianbian.net/%E4%B8%AD%E9%97%B4%E6%B7%BB%E5%8A%A0%E5%85%83%E7%B4%A0.gif)

#### 删除元素

1. 删除末尾元素

   **场景：** 存在长度为6的数组，存在A、B、C三个值，把C值（索引为2）从数组中删除；

   **步骤：** 直接进行删除，然后挪动size；

   ![删除末尾元素](http://mkstatic.lianbian.net/%E5%88%A0%E9%99%A4%E6%9C%AB%E5%B0%BE%E5%85%83%E7%B4%A0.gif)

2. 删除中间元素

   **场景：** 存在长度为6的数组，存在A、B、C三个值，把B值（索引为1）从数组中删除；

   **步骤：** 找到指定索引的值，进行删除，把删除元素后边的元素依次往前挪，把最后一个元素设置为null，让他释放垃圾，然后移动size；

   

   ![删除中间元素](http://mkstatic.lianbian.net/%E5%88%A0%E9%99%A4%E4%B8%AD%E9%97%B4%E5%85%83%E7%B4%A0.gif)

#### 修改元素

**场景：** 存在长度为6的数组，存在A、B、C三个值，把B值（索引为2）修改成D值；

**步骤：** 找到指定索引，把原来的值进行赋值，把新元素直接覆盖，返回oldValue；

![修改元素](http://mkstatic.lianbian.net/%E4%BF%AE%E6%94%B9%E5%85%83%E7%B4%A0.gif)

#### 查询元素

**场景：** 存在长度为6的数组，存在A、B、C三个值，把B值（索引为2）；

**步骤：** 找到指定索引，返回；

![获取元素](http://mkstatic.lianbian.net/%E8%8E%B7%E5%8F%96%E5%85%83%E7%B4%A0.gif)

#### 动态扩容

```ArrayList```是一种以数组实现的```List```，与数组相比，增删改查元素都一样，也需要**连续的内存空间**，但是它具有动态扩展的能力，因此也可称之为```动态数组```。

这里着重用动图表示动态数组的**扩容机制**，因为增删改查元素和数组是一样的，可以参照数组的动态图。

**场景：** 存在长度为4的数组，存在A、B、C、D四个值，现在需要在最后添加元素，原来的长度不够，触发扩容机制；

**步骤：** 按照原来数组长度的**1.5倍**创建一个新数组，即创建size=6的数组，把原来的old数组平移到新数组，size也进行同步平移，进行添加元素，然后修改数组的引用（由原来的old数组引用改成new数组引用）；

![数组扩容](http://mkstatic.lianbian.net/%E6%95%B0%E7%BB%84%E6%89%A9%E5%AE%B91.gif)

### 数组特性与总结

1. 需要**连续的内存空间**来储存；
2. 添加元素的性能，与添加元素位置有直接关系，末尾添加效率很高，越往前走效率越低（因为要移动元素），所以在**不确定的添加位置场景下，不适合用数组**，而储存固定大小的同类型的元素，可以选择用数组；
3. **查找效率高**，根据索引能直接找出对应元素返回，找不到抛出对应异常；
4. 扩容是按照**原来数组容量（capacity）的一半**扩容；
5. 扩容是创建一个新数组，然后平移复制原来数组的方式，而不是直接在原来的数组后边直接扩容，这点是由于数组需要连续的内存空间决定的；



通过上边的原理分析与总结，我们来**实战面试题**，来测试理论的掌握程度。

## 实战面试题

1. ArrayList的size和capacity怎么理解？
2. ArrayList内部是怎么存放数据的？
3. ArrayList的大小是如何自动扩容的？
4. 在索引中ArrayList的增加或者删除某个对象的运行过程？效率很低吗？解释一下为什么？
5. ArrayList中的remove是如何操作的？
6. ArrayList的add操作优化？



这是数组常见的面试题，你能手到擒来吗？

在文章的末尾会给出标准的答案，有不懂的题目也不着急看答案，我们继续**阅读源码**来巩固与思考。

## 源码分析类结构

源码基于```jdk1.8```，首先来一段```ArrayList.java```类的介绍：

1. 代码总行数```1469```行；
2. 实现了```AbstractList<E>```抽象类；
3. 继承```List<E>, RandomAccess, Cloneable, java.io.Serializable```；

## 源码方法分类讲解

先看下这张图：

![ArrayList构造方法和公用方法](http://mkstatic.lianbian.net/ArrayListPublicMethod.png)

有没有一下就懵了？不着急，我把这个类的方法**分类**一下：

![方法分类点击查看大图](http://mkstatic.lianbian.net/ArrayListClass1.png)

这篇文章从**继承、实现、属性、构造方法、常用方法**（其他方法类不讲解）来分类讲解。

这里重复贴一下ArrayList类图。

![ArrayList类图](http://mkstatic.lianbian.net/ArrayList-20210811144456212-20210811190012060.png)

### 继承

通过上边类图，你会发现ArrayList继承了```AbstractList```抽象类，AbstractList实现了List接口，而AbstractList抽象类有且只有一个抽象方法：

```java
abstract public E get(int index);
```

这里看源码的同学绝对有个疑问，为什么AbstractList实现了List接口，而我们的ArrayList也再次来实现了List，为什么会这样子设计呢？

```java
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable{}

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>{}
```

**猜测有三：**

1. 增加可读性，不要套娃一样的去看源码，可以清晰看到类的主要实现接口；
2. 兼容问题；
3. 面向接口编程；



### 实现

ArrayList实现了```List, RandomAccess, Cloneable, java.io.Serializable```接口；

ArrayList实现了List，提供了基础的添加、删除、遍历等操作；

ArrayList实现了RandomAccess，提供了随机访问的能力；

ArrayList实现了Cloneable，可以被克隆；

ArrayList实现了Serializable，可以被序列化；



### 属性

```java
private static final long serialVersionUID = 8683452581122892189L;

/**
 * 默认容量
 */
private static final int DEFAULT_CAPACITY = 10;

/**
 * 空数组，如果传入的容量为0时使用
 */
private static final Object[] EMPTY_ELEMENTDATA = {};

/**
 * 空数组，传传入容量时使用，添加第一个元素的时候会重新初始为默认容量大小
 */
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

/**
 * 存储元素的数组
 */
transient Object[] elementData; // non-private to simplify nested class access

/**
 * 集合中元素的个数
 */
private int size;
```

**serialVersionUID** 是用来验证版本一致性的字段。我们将一个类的二进制字节序列转为java对象，也就是反序列化时，JVM会把传进来的二进制字节流中的serialVersionUID和本地相应的实体或对象的serialVersionUID进行比较，如果相同，则认为两个类是一致的，可以进行反序列化，否则就会出现版本不一致的反序列化异常。

**DEFAULT_CAPACITY** 默认容量为10，也就是通过new ArrayList()创建时的默认容量。

**EMPTY_ELEMENTDATA** 空的数组，这种是通过new ArrayList(0)创建时用的是这个空数组（下边构造方法代码中可以看到）。

**DEFAULTCAPACITY_EMPTY_ELEMENTDATA** 也是空数组，这种是通过new ArrayList()创建时用的是这个空数组，与EMPTY_ELEMENTDATA的区别是在添加第一个元素时使用这个空数组的会初始化为DEFAULT_CAPACITY（10）个元素。

**elementData** 真正存放元素的地方，使用transient是为了不序列化这个字段，至于没有使用private修饰，后面注释是写的“为了简化嵌套类的访问”，但是楼主实测加了private嵌套类一样可以访问，private表示是类私有的属性，只要是在这个类内部都可以访问，**嵌套类或者内部类也是在类的内部，所以也可以访问类的私有成员。**

**size** 真正存储元素的个数，而不是elementData数组的长度（capacity）。



### 构造方法

**ArrayList(int initialCapacity)构造方法**

传入初始容量，如果大于0就初始化elementData为对应大小，如果等于0就使用EMPTY_ELEMENTDATA空数组，如果小于0抛出异常。

```java
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        // 如果传入的初始容量大于0，就新建一个数组存储元素
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        // 如果传入的初始容量等于0，使用空数组EMPTY_ELEMENTDATA
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        // 如果传入的初始容量小于0，抛出异常
        throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }
}
```

**ArrayList()构造方法**

不传初始容量，初始化为DEFAULTCAPACITY_EMPTY_ELEMENTDATA空数组，会在添加第一个元素的时候扩容为默认的大小，即10。

```java
public ArrayList() {
    // 如果没有传入初始容量，则使用空数组DEFAULTCAPACITY_EMPTY_ELEMENTDATA
    // 使用这个数组是在添加第一个元素的时候会扩容到默认大小10
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
```

**ArrayList(Collection<? extends E> c)构造方法**

传入集合并初始化elementData，这里会使用拷贝把传入集合的元素拷贝到elementData数组中，如果元素个数为0，则初始化为EMPTY_ELEMENTDATA空数组。

```java
/**
* 把传入集合的元素初始化到ArrayList中
*/
public ArrayList(Collection<? extends E> c) {
    // 集合转数组
    elementData = c.toArray();
    if ((size = elementData.length) != 0) {
        // 检查c.toArray()返回的是不是Object[]类型，如果不是，重新拷贝成Object[].class类型
				// c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (elementData.getClass() != Object[].class)
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
        // 如果c的空集合，则初始化为空数组EMPTY_ELEMENTDATA
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```

为什么`c.toArray();`返回的有可能不是Object[]类型呢？主要还是因为jdk自身的bug导致的。

源码注释：c.toArray might (incorrectly) not return Object[] (see 6260652)

https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6260652

请看下面的代码：

```java
public class ArrayTest {
    public static void main(String[] args) {
        Father[] fathers = new Son[]{};
        // 打印结果为class [Lcom.coolcoding.code.Son;
        System.out.println(fathers.getClass());

        List<String> strList = new MyList();
        // 打印结果为class [Ljava.lang.String;
        System.out.println(strList.toArray().getClass());
    }
}

class Father {}

class Son extends Father {}

class MyList extends ArrayList<String> {
    /**
     * 子类重写父类的方法，返回值可以不一样
     * 但这里只能用数组类型，换成Object就不行
     * 应该算是java本身的bug
     */
    @Override
    public String[] toArray() {
        // 为了方便举例直接写死
        return new String[]{"1", "2", "3"};
    }
}
```

### 常用方法（增删改查）

**add(E e)方法**

添加元素到末尾，平均时间复杂度为O(1)。

```java
public boolean add(E e) {
    // 检查是否需要扩容
    ensureCapacityInternal(size + 1);
    // 把元素插入到最后一位
    elementData[size++] = e;
    return true;
}

private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

private static int calculateCapacity(Object[] elementData, int minCapacity) {
    // 如果是空数组DEFAULTCAPACITY_EMPTY_ELEMENTDATA，就初始化为默认大小10
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        return Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    return minCapacity;
}

private void ensureExplicitCapacity(int minCapacity) {
    modCount++;

    if (minCapacity - elementData.length > 0)
        // 扩容
        grow(minCapacity);
}

private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    // 新容量为旧容量的1.5倍
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    // 如果新容量发现比需要的容量还小，则以需要的容量为准
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    // 如果新容量已经超过最大容量了，则使用最大容量
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    // 以新容量拷贝出来一个新数组
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

步骤：

1. 检查是否需要扩容；
2. 如果elementData等于DEFAULTCAPACITY_EMPTY_ELEMENTDATA则初始化容量大小为DEFAULT_CAPACITY；
3. 新容量是老容量的1.5倍（oldCapacity + (oldCapacity >> 1)），如果加了这么多容量发现比需要的容量还小，则以需要的容量为准；
4. 创建新容量的数组并把老数组拷贝到新数组；



**add(int index, E element)方法**

添加元素到指定位置，平均时间复杂度为O(n)。

```java
public void add(int index, E element) {
    // 检查是否越界
    rangeCheckForAdd(index);
    // 检查是否需要扩容
    ensureCapacityInternal(size + 1);
    // 将inex及其之后的元素往后挪一位，则index位置处就空出来了
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
    // 将元素插入到index的位置
    elementData[index] = element;
    // 大小增1
    size++;
}

private void rangeCheckForAdd(int index) {
    if (index > size || index < 0)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```

步骤：

1. 检查索引是否越界；
2. 检查是否需要扩容；
3. 把插入索引位置后的元素都往后挪一位；
4. 在插入索引位置放置插入的元素；
5. 大小加1；

**remove(int index)方法**

删除指定索引位置的元素，时间复杂度为O(n)。

```java
public E remove(int index) {
    // 检查是否越界
    rangeCheck(index);

    modCount++;
    // 获取index位置的元素
    E oldValue = elementData(index);
    
    // 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    
    // 将最后一个元素删除，帮助GC
    elementData[--size] = null; // clear to let GC do its work

    // 返回旧值
    return oldValue;
}
```

步骤：

1. 检查索引是否越界；
2. 获取指定索引位置的元素；
3. 如果删除的不是最后一位，则其它元素往前移一位；
4. 将最后一位置为null，方便GC回收；
5. 返回删除的元素。

可以看到，ArrayList删除元素的时候并没有缩容。

**remove(Object o)方法**

删除指定元素值的元素，时间复杂度为O(n)。

```java
public boolean remove(Object o) {
    if (o == null) {
        // 遍历整个数组，找到元素第一次出现的位置，并将其快速删除
        for (int index = 0; index < size; index++)
            // 如果要删除的元素为null，则以null进行比较，使用==
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        // 遍历整个数组，找到元素第一次出现的位置，并将其快速删除
        for (int index = 0; index < size; index++)
            // 如果要删除的元素不为null，则进行比较，使用equals()方法
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}
```

步骤：

循环调用fastRemove(int index)

**fastRemove(int index)**

```java
private void fastRemove(int index) {
    // 少了一个越界的检查
    modCount++;
    // 如果index不是最后一位，则将index之后的元素往前挪一位
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    // 将最后一个元素删除，帮助GC
    elementData[--size] = null; // clear to let GC do its work
}
```

步骤：

1. 找到第一个等于指定元素值的元素；
2. 快速删除；

fastRemove(int index)相对于remove(int index)少了检查索引越界的操作，可见jdk将性能优化到极致。

**set(int index, E element)方法**

修改指定元素值的元素，时间复杂度为O(1)。

```java
public E set(int index, E element) {
  // 检查是否越界
	rangeCheck(index);
  
  // 获取index位置的元素
  E oldValue = elementData(index);
  
  // 设置index位置的元素
  elementData[index] = element;
  
  // 返回
  return oldValue;
}

private void rangeCheck(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```

步骤：

1. 检查索引是否越界，这里只检查是否越上界，如果越上界抛出IndexOutOfBoundsException异常，如果越下界抛出的是ArrayIndexOutOfBoundsException异常。
2. 获取指定索引的位置的元素，赋值给oldValue，用于返回
3. 设置指定索引位置的元素
4. 返回oldValue

**get(int index)方法**

获取指定索引位置的元素，时间复杂度为O(1)。

```java
public E get(int index) {
    // 检查是否越界
    rangeCheck(index);
    // 返回数组index位置的元素
    return elementData(index);
}

private void rangeCheck(int index) {
    if (index >= size)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

E elementData(int index) {
    return (E) elementData[index];
}
```

步骤：

1. 检查索引是否越界，这里只检查是否越上界，如果越上界抛出IndexOutOfBoundsException异常，如果越下界抛出的是ArrayIndexOutOfBoundsException异常。
2. 返回索引位置处的元素；



## 源码总结

1. ArrayList内部使用数组存储元素，当数组长度不够时进行扩容，每次加一半的空间，ArrayList不会进行缩容；
2. ArrayList支持随机访问，通过索引访问元素极快，时间复杂度为O(1)；
3. ArrayList添加元素到尾部极快，平均时间复杂度为O(1)；
4. ArrayList添加元素到中间比较慢，因为要搬移元素，平均时间复杂度为O(n)；
5. ArrayList从尾部删除元素极快，时间复杂度为O(1)；
6. ArrayList从中间删除元素比较慢，因为要搬移元素，平均时间复杂度为O(n)；



## 面试题答案

**ArrayList的size和capacity怎么理解？**

如果把 ArrayList 看作一个杯子的话，capacity 就是杯子的容积，也就是代表杯子能装多少东西，而 size 就是杯子装的东西的体积。杯子可能装满了，也可能没装满，所以 capacity >= size 。capacity 过大和过小都不好，过大会造成浪费，过小又存放不下多个元素的值，capacity == size，则 ArrayList 空间利用率最大，但是不利于添加新的元素。当 ArrayList 实例内的元素个数不再改变了，可以使用 trimToSize() 方法最小化 ArrayList 实例来节省空间，也即是使 capacity == size。

**ArrayList内部是怎么存放数据的？**

ArrayList 可以看做是数组的封装，使用 elementData 数组来存储数据，使用 size 来代表 elementData 的非 null 元素个数。elementData 前没有访问修饰符，所以只有同类和同包下的类可以直接方法，外界想要知道 ArrayList 实例内元素的个数就要通过 size 属性。elementData 数组类型是 Object 类型，可以存放任意的引用类型，不能存放基本的数据类型。

**ArrayList的大小是如何自动扩容的？**

扩容是发生在添加操作前的，要保证要添加元素在 elementData 数组中有位置，也即是 size 加上要添加的元素个数要小于 capacity（size + num <= capacity 就说明容量是充足的），所以在添加方法中，先调用 ensureCapacityInternal(int) 方法来确保 elementData 容量充足，然后再进行具体的添加操作。如果 ensureCapacityInternal 方法（ensureCapacityInternal 方法中有调用了其他方法）发现数组容量不够了，就会扩容。扩容实际的方法是 grow(int) 方法，使用位运算符来使数组的容量扩容 1.5 倍。但是需要注意的是，没有指定初始化值的 ArrayList 实例，第一次扩容并不是以 1.5 倍扩容的，而是使用的默认容量 10，所以网上很多直接说 ArrayList 扩容是 1.5 倍也有不当之处，这点从 JDK 源码中可以很明确的看出来。

如果在构造 ArrayList 实例时，指定初始化值（初始化容量或者集合），那么就会创建指定大小的 Object 数组，并把该数组对象的引用赋值给 elementData；如果不指定初始化值，在第一次添加元素值时会使用默认的容量大小 10 作为 elementData 数组的初始容量，使用 Arrays.conpyOf() 方法创建一个 Object[10] 数组。

**在索引中ArrayList的增加或者删除某个对象的运行过程？效率很低吗？解释一下为什么？**

其实通过上边的源码可以知道，我们要分情况来讨论，详情见源码总结3、4、5、6点。

**ArrayList中的remove是如何操作的？**

见源码remove解析。

**ArrayList的add操作优化？**

核心就是避免 ArrayList 内部进行扩容。

​	1、对于普通少量的 add 操作，如果插入元素的个数已知，最好使用带初始化参数的构造方法，避免 ArrayList 内部再进行扩容，提高性能。

​	2、对于大量的 add 操作，最好先使用 ensureCapacity 方法来确保 elementData 数组中有充足的容量来存放我们后面 add 操作的元素，避免 ArrayList 实例内部进行扩容。上面提到的 ensureCapacityInternal 方法是一个私有方法，不能直接调用，而 ensureCapacity 方法是一个共有方法，专门提供给开发者使用的，提高大量 add 操作的性能。



我是连边，专注于Java和架构领域，坚持撰写有原理，有实战，有体系的技术文章。

可以关注`订阅号@连边`不错过精彩文章

![订阅号@连边](http://mkstatic.lianbian.net/202203082219869.jpg)