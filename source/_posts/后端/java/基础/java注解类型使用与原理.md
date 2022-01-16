---
title: java注解类型使用与原理
date: 2021-08-02
tags: java基础
categories: java基础
---

# 前言

每当我们在敲代码的时候，按下``@Data``，在```lombok```插件帮助下，就把繁琐的```get set```工作做得服服帖帖；而敲下 ```@Test```，也能很方便的右键菜单运行我们的单元测试程序 ...

你会不会觉得这些玩意儿，真神奇。

今天给大家讲的主角是```注解```。

注解，是Java中最重要的，但是却最容易被人***遗忘***的知识点。

很多时候，就理所当然的觉得它就一直是那样子的；

哪怕```Spring```、```SpringMVC```、```SpringBoot```等框架中充满了```注解```，还是选择性地忽视它。

很多时候，不明白它是怎么起作用的，甚至把它和 **注释 **混淆 ...

我们在工作中机械性地在```Controller```上加 ```@RequestMapping```。

想彻底弄清楚注解吗？

通过本文，***循序渐进***的彻底搞明白***注解***。



# 为什么要有注解

如果早期用```xml bean```来配置来管理过类对象的相信有很深的感触，到项目的开发后期，开发人员都不知道什么时候开始，发现```xml```的维护越来越糟糕。

而使用注解，能够让配置与代码维持一个很好的平衡（不过份耦合也不过份松散），而且也能 **提高我们代码的阅读性**，比如我看到 ```@Test```注解，会很自然的知道这个注解是用来做测试功能的；

还有一个潜在优势，就是和注解和注释一样，它的位置信息就能表示它的**作用域**；

例如下边这个```@Test``` 注解，我们一看就知道在 ```funUnit``` 来作用，而实际也是如此。

```java
@Test
public void funUnit {
   // 这行注释给下边这行的
}
```

因为有自定义注解的存在，所以也能方便我们扩展配置与功能；

所以呢，为什么我们需要注解，就是基于几点为初衷：

1. 让我们从繁琐的配置文件中抽离出来；
2. 提高代码阅读性；
3. 自定义注解能够方便我们扩展配置与功能；



# 注解的定义

引用维基百科的定义：

> Java注解又称Java标注，是JDK5.0版本开始支持加入源代码的特殊语法 **元数据** 。
>
> Java语言中的类、方法、变量、参数和包等都可以被标注。和Javadoc不同，Java标注可以通过反射获取标注内容。在编译器生成类文件时，标注可以被嵌入到字节码中。Java虚拟机可以保留标注内容，在运行时可以获取到标注内容。 当然它也支持自定义Java标注。



连边的一句话定义：

> 在指定的位置上放上一个记号，然后在这个记号的里边，可以有这个记号自己的属性，然后规定一些规则。（如：在什么地方可以放这个标记，在什么时候可以获取到这个标记。）



暂时不理解这句话没事，我们先补充一些注解的基础知识。



# 注解的语法

## 注解分类

1. 自定义注解：自己写的注解，如我之前那篇文章，自定义的 ```@UserSiteMail、@PayCode``` 注解；
2. JDK内置注解：如```@Override```检验方法重写，```@Deprecated```标识方法过期等
3. 还有第三方框架提供的注解：SpringMVC的```@Controller```



## 注解语法

```java
@Retention
@Target(ElementType.METHOD)
public @interface 注解名称{
    属性列表;
}
```

上边这段代码， 有两个***元注解（注解的注解）***，它属于***JDK内置注解***

### 元注解

```@Target``` ：加在注解上，限定该注解能放的位置。

```java
类或接口：ElementType.TYPE；
字段：ElementType.FIELD；
方法：ElementType.METHOD；
构造方法：ElementType.CONSTRUCTOR；
方法参数：ElementType.PARAMETER
  
定义多个：
@Target({
	ElementType.METHOD,
  ElementType.FIELD
})
```



```@Retention``` ：用来规定注解的保留策略，就是在哪个阶段，我们需要保留我们定义的这些标记。

```java
SOURCE：在源码中保留，我们要保留标记，比如@Override，@FunctionalInterface，一般编译器语法检查的，都在源码级别保留；
ClASS：在class文件中保留注解，如果@Retention不存在，则该注解默认为CLASS；
RUNTIME：通常我们自定义的Annotation都是RUNTIME，因为我们一般应用注解都是在反射的时候来处理业务逻辑；
```

![保留策略（图片来源网络）](http://mkstatic.lianbian.net/v2-14ec3964feb69f2e8ed2dc4a2e90d6a2_720w.jpg)



## 完整语法示例

```java
// 因为要通过反射处理业务，所以定义在运行时
@Retention(RetentionPolicy.RUNTIME)  
// 类或者接口上使用
@Target(ElementType.TYPE)  
public @interface PayCode {  

     String value();    
     String name();  
}

// 使用的地方
@PayCode(value = "alia", name = "支付宝支付")  
@Service  
public class AliaPay implements IPay {  

     @Override  
     public void pay() {  
         System.out.println("===发起支付宝支付===");  
     }  
}  

 
@PayCode(value = "weixin", name = "微信支付")  
@Service  
public class WeixinPay implements IPay {  
 
     @Override  
     public void pay() {  
         System.out.println("===发起微信支付===");  
     }  
} 

 
@PayCode(value = "jingdong", name = "京东支付")  
@Service  
public class JingDongPay implements IPay {  
 
     @Override  
     public void pay() {  
        System.out.println("===发起京东支付===");  
     }  
}
```



还记得前边连边定义的那句话吗？



> 在指定的位置上放上一个记号，然后在这个记号的里边，可以有这个记号自己的属性，然后规定一些规则。（如：在什么地方可以放这个标记，在什么时候可以获取到这个标记。）



嵌套到这个注解中来翻译一下：

我在各种支付方式的地方，放上一个记号，记号里设置自己的属性（alia、weixin、jingdong），在类或者接口上可以放这个记号（@Target(ElementType.TYPE)  ），这个标记在运行时的时候获取到（@Retention(RetentionPolicy.RUNTIME)  ）。



# 注解的本质与原理



## 本质

注解本质是一个继承了`Annotation` 的特殊接口，这里可以通过class文件看出；

查看class文件方式（简单的写一下步骤，不清楚的可以```google```）：

1. IDEA左上角打开File—Project Structure
2. 在Project Settings — Modules 下找到我们的项目
3. 点击右侧的Paths ， 查看Output Path位置
4. 复制路径，在我的电脑中粘贴路径即可找到对应文件夹
5. ```javap -v``` 粘贴class文件路径

查看上面```payCode```注解class文件如下：

![payCode class文件](http://mkstatic.lianbian.net/image-20210803153300329.png)



美化一下：

```java
public interface PayCode extends Annotation {
  
}
```



## 原理

接口本质的具体实现类是```Java 运行时```生成的动态代理类。而我们通过反射获取注解时，返回的是Java 运行时生成的动态代理对象`$Proxy1`。通过代理对象调用自定义注解（接口）的方法，会最终调用`AnnotationInvocationHandler` 的`invoke`方法。该方法会从`memberValues` 这个Map 中索引出对应的值。而`memberValues` 的来源是Java 常量池。



# 注解实战

这里贴上我上一篇文章（）里边的项目实战，利用注解，优雅的避免了冗长的 ```if..else...```

```java
@PayCode(value = "alia", name = "支付宝支付")  
@Service  
public class AliaPay implements IPay {  

     @Override  
     public void pay() {  
         System.out.println("===发起支付宝支付===");  
     }  
}  

 
@PayCode(value = "weixin", name = "微信支付")  
@Service  
public class WeixinPay implements IPay {  
 
     @Override  
     public void pay() {  
         System.out.println("===发起微信支付===");  
     }  
} 

 
@PayCode(value = "jingdong", name = "京东支付")  
@Service  
public class JingDongPay implements IPay {  
 
     @Override  
     public void pay() {  
        System.out.println("===发起京东支付===");  
     }  
}

@Service  
public class PayService implements ApplicationListener<ContextRefreshedEvent> {  
 
     private static Map<String, IPay> payMap = null;  
     
     @Override  
     public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {  
         ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();  
         Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(PayCode.class);  
        
         if (beansWithAnnotation.size > 0) {  
             payMap = new HashMap<>();  
             beansWithAnnotation.forEach((key, value) ->{  
                 String bizType = value.getClass().getAnnotation(PayCode.class).value();  
                 payMap.put(bizType, (IPay) value);  
             });  
         }  
     }  
    
     public void pay(String code) {  
        payMap.get(code).pay();  
     }  
}
```



https://www.likecs.com/show-20596.html



https://www.zhihu.com/question/24401191