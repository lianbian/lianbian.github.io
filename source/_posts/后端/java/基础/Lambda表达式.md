---
title: Lambda表达式
date: 2021-07-08
tags: java8实战
categories: java基础
---

# 文章概述

这篇文章会循序渐进的讲解为什么我们需要lambda，包括两个方面：

1. 案例：从农场主苹果过滤案例循序渐进的引入概念；
2. lambda实战；



# 案例

## 农场主苹果过滤案例

Apple 实体类（包括颜色和重量两个属性）：

```java
import lombok.Data;

@Data
public class Apple {
    public Apple(int weight, String color) {
        this.color = color;
        this.weight = weight;
    }

    private String color;
    private int weight;
}
```

苹果列表集合

```java
List<Apple> apples = Array.asList(
  new Apple("green", 100), // 定义：重量大于100克的绿色苹果
  new Apple("red", 110),  // 定义：重量大于110克的红色苹果
  new Apple("green", 111) // 定义：重量大于111克的绿色苹果
);
```

### 需求1：筛选过滤出绿色的苹果

```java
// 接口定义
List<Apple> filterGreenApples(List<Apple> apples);

// 代码实现
List<Apple> filterGreenApples(List<Apple> apples) {
  List<Apple> greenApples = new ArrayList<>();
  for(Apple apple:apples) {
    if(apple.getColor().equals("green")) {
      greenApples.add(apple);
    }
  }

  return greenApples;
}

// 打印测试
List<Apple> greenApples = filterGreenApples(apples);
System.out.println(greenApples);
```

### 需求2：筛选过滤出红色的苹果

```java
// 接口定义
List<Apple> filterRedApples(List<Apple> apples);

// 方法实现
List<Apple> filterRedApples(List<Apple> apples) {
  List<Apple> redApples = new ArrayList<>();
  for(Apple apple:apples) {
    if(apple.getColor().equals("red")) {
      redApples.add(apple);
    }
  }

  return redApples;
}

// 打印测试
redApples = filterRedApples(apples);
System.out.println(redApples);
```

### 需求3：筛选过滤出重量大于100克的苹果

```java
// 接口定义
List<Apple> filterGt100Apples(List<Apple> apples);

// 代码实现
List<Apple> filterGt100Apples(List<Apple> apples) {
  List<Apple> gt100Apples = new ArrayList()<>;
  for(Apple apple:apples) {
    if(apple.getWeight() > 100) {
      gt100Apples.add(apple);
    }
  }

  return gt100Apples;
}

// 打印测试
List<Apple> gt100Apples = filterGt100Apples(apples);
System.out.println(gt100Apples);
```

### 需求4：筛选过滤出绿色并且重量大于100克的苹果

```java
// 接口定义
List<Apple> filterGreenAndGt100Apples(List<Apple> apples);

// 方法实现
List<Apple> filterGreenAndGt100Apples(List<Apple> apples) {
  List<Apple> greenAndGt100Apples = new ArrayList<>();
  for(Apple apple:apples) {
    if(apple.getColor().equals("green") && apple.getWeight() > 100) {
      greenAndGt100Apples.add(apple);
    }
  }

  return greenAndGt100Apples;
}

// 打印测试
List<Apple> greenAndGt100Apples = filterGreenAndGt100Apples(apples);
System.out.println(greenAndGt100Apples);
```



## 思考

### 优点

1. 逻辑简单；
2. 如果需求固定，这种实现方式未尝不可；

### 缺点

1. 重复的代码多；
2. 不方便维护与扩展；
3. 从人的思维的角度，代码的阅读性不高；
4. ... 自行补充

### 改进

#### 本质

“给予执行方法一本说明书，而不是具体的执行的一系列参数。"

改进的本质：让方法的参数具有行为能力。

#### 策略模式

```java
// 一、定义策略的抽象接口
// 释义：你给我一个苹果（苹果实例），这个是否符合我的规定（判断函数），我来告诉你（返回值）。
interface ApplePredicate {
    boolean test(Apple apple);
}

// 二、定义具体的策略
// 绿色苹果策略
class greenApplePredicate implements ApplePredicate {
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

// 红色苹果策略
class redApplePredicate implements ApplePredicate {
    @Override
    public boolean test(Apple apple) {
        return apple.getWeight() > 150;
    }
}

// 找出大于100克的苹果（省略）
// 找出绿色的大于100克的苹果（省略）

// 三、利用策略来过滤
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (p.test(apple)) {
      result.add(apple);
    }
  }

  return result;
}

// 打印测试

// 通过策略的方式，筛选出绿色苹果
List<Apple> greenApples = filterApples(apples, new greenApplePredicate());
System.out.println(greenApples);

// 通过策略的方式，筛选出红色苹果
List<Apple> redApples = filterApples(apples, new redApplePredicate());
System.out.println(redApples);
```



#### 匿名内部类

```java
// 一、定义策略的抽象接口
// 释义：你给我一个苹果（苹果实例），这个是否符合我的规定（判断函数），我来告诉你（返回值）。
interface ApplePredicate {
    boolean test(Apple apple);
}

ApplePredicate applePredicate = new ApplePredicate() {

  @Override
  public boolean test(Apple apple) {
		// 执行具体的业务
  }
};

applePredicate.test(apple);
```



#### Lambda表达式

```java
// 筛选绿色苹果
List<Apple> greenApples = apples.stream().filter(apple -> apple.getColor().equals("green")).collect(Collectors.toList());

// 筛选出红色苹果
List<Apple> redApples = apples.stream().filter(apple -> apple.getColor().equals("red")).collect(Collectors.toList());
```



### 讨论：各种方式实现的优缺点

1. 策略模式：方便扩展，灵活应对需求的变化；
2. 结构清晰，思维语意化；
3. 适应改变（自己就是方法的本体，不需要刻意的去封装单独的函数）；
4. lambda：更从人的思维点出发，看着代码基本能了解这个代码是实现什么目标；
5. lambda：节省内存空间（java7 和 java8 内存分配实战，内部类和lambda字节码更节省内存空间）；
6. ... 



# lambda实战

## lambda的语法

| 参数             | 符号  | 表达式                                          |
| ---------------- | ----- | ----------------------------------------------- |
| parameter list   | arrow | lambda body                                     |
| (apple1, apple2) | ->    | apple1.getColor().compareTo(apple2.getColor()); |

## 函数式接口

### 什么是函数式接口

1. 接口有且只有一个抽象方法；
2. 允许定义静态的非抽象方法；
3. 允许定义默认defalut非抽象方法；
4. 允许定义java.lang.Object中的public方法；
5. @FunctionalInterface注解不是必须的；（在编译期间，会执行是否是函数式接口检查）

### 常见一些函数式接口

```java
// 判断，如上文的策略模式
Predicate boolean test(T t);
// 无返回值
Consumer accept(T t);
// 输入、输出、返回值
Function<T, R> R apply(T t); 
// 创建一个对象
Supplier<T> T get();
```

## 内置的函数式接口

### Predicate

```java
List<Apple> filter(List<Apple> apples, Predicate<Apple> predicate) {
   List<Apple> result = new ArrayList<>();
   for(Apple a:apples) {
      if(predicate.test(a)) 
         result.add(a);
   }
   return result;
};

// LongPredicate
public List<Apple> filterByWeight(List<Apple> apples, LongPredicate predicate) {
  List<Apple> result = new ArrayList<>();
  for (Apple a : apples) {
    if (predicate.test(a.getWeight()))
      result.add(a);
  }
  return result;
}

filterByWeight(list, w -> w > 100);

// BiPredicate 两个参数的输入
public List<Apple> filterByWeightandColor(List<Apple> apples, BiPredicate<String, Integer> predicate) {
  List<Apple> result = new ArrayList<>();
  for (Apple a : apples) {
    if (predicate.test(a.getColor(), a.getWeight())) {
      result.add(a);
    }
  }
  return result;
}

List<Apple> filterByWeightandColor(list, (s, w) -> s.equals("green") && w>100);
```

### consumer

```java
// Consumer
List<Apple> simpleConsumer(List<Apple> apples, Consumer<Apple> consumer) {
  for(Apple a : apples) {
      consumer.accept(a);
  }
}
simpleConsumer(apples, a -> System.out.prinln(a));

// BiConsumer
List<Apple> simpleBiConsumer(String c, List<Apple> apples, BiConsumer<Apple, String> consumer) {
  for(Apple a : apples) {
      consumer.accept(a);
  }
}
simpleBiConsumer("xxx", apples, (a, s) -> System.out.prinln(s + a.getC()));
```

### function

```java
// Function
Apple testFun(Apple apple, Function<Apple, String> fun) {
  return apple.apply(apple);
}

String result3 = testFun(new Apple("yellow", 100), (a)-> a.toString())
System.out.prinln(result3);

// BiFunction
// 自定义Function
```

### supplier 

```java
// Supplier
Supplier<String> s = String::new; // method inference
System.out.println(s.get().getClass());
```

## 方法推导 Method references

```java
Consumer<String> consumer = (s) -> System.out.println(s);
useConsumer(consumer, "Hello Alex");

private static <T> void useConsumer(Consumer<T> consumer, T t) {
		consumer.accept(t);
  	consumer.accept(t);
}

// 方法推导，更加简单的实现
useConsumer(s -> System.out.println(s), "Hello Alex");
useConsumer(System.out::println, "Hello Alex");

// 什么情况下可以进行方法推导
Function<String, Interger> f = Integer::parseInt;
Integer result = f.apply("123");
System.out.println(result);

String string = new String("Hello");
Function<Integer, Character> f3 = String::charAt;
Character c2 = f3.apply(4);
System.out.println(c2);
```
