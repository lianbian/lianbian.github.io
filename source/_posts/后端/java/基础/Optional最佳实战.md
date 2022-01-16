---
title: Optional最佳实战
date: 2021-07-24
tags: java基础
categories: java
---

# Optional

Java 从 1.8 之后引入了 optional API，用以一定程度地避免 NPE（*NullPointerException*）。然而可能很多人对这个精心设计的 API 还没那么熟练，甚至会有一些滥用。下面会详细讨论，然后给出我所认为的最佳实践。

## 来自作者的说明

首先我们来看一下`Optional`的作者 Brian Goetz 对这个 API 的说明：

> Our intention was to provide a limited mechanism for library method return types where there needed to be a clear way to represent "no result", and using `null` for such was overwhelmingly likely to cause errors.

大意为，为了避免`null`带来的错误，我们提供了一个可以**明确**表示空值的有限的机制。

## 基础理解

首先，`Optional`是一个容器，用于放置可能为空的值，它可以合理而优雅的处理`null`。众所周知，`null`在编程历史上极具话题性，号称是*计算机历史上最严重的错误*，感兴趣可以读一下这篇文章：**[THE WORST MISTAKE OF COMPUTER SCIENCE](https://www.lucidchart.com/techblog/2015/08/31/the-worst-mistake-of-computer-science/)**，这里暂且不做过多讨论。在 Java 1.8 之前的版本，没有可以用于表示`null`官方 API，如果你足够的谨慎，你可能需要常常在代码中做如下的判断：

```
if (null != user) {    //doing something}if (StringUtil.isEmpty(string)) {    //doing something}
```

复制代码

确实，返回值是`null`的情况太多了，一不小心，就会产生 NPE，接踵而来的就是应用运行终止，产品抱怨，用户投诉。

1.8 之后，jdk 新增了`Optional`来表示空结果。其实本质上什么也没变，只是增加了一个表达方式。`Optional`表示空的静态方法为`Optional.empty()`，跟`null`有什么本质区别吗？其实没有。翻看它的实现，`Optional`中的 value 就是`null`，只不过包了一层`Optional`，所以说它其实是个容器。用之后的代码可能长这样：

```
// 1Optional<User> optionalUser = RemoteService.getUser();if (!optionalUser.isPresent()) {   //doing something }User user = optionalUser.get();
// 2User user = optionalUser.get().orElse(new User());
```

复制代码

看起来，好像比之前好了一些，至少看起来没那么笨。但如果采用写法 1，好像更啰嗦了。

如果你对 kotlin 稍有了解，kotlin 的非空类型是他们大肆宣传的"卖点"之一，通过`var param!!`在使用它的地方做强制的空检查，否则无法通过编译，最大程度上减少了 NPE。其实在我看来，`Optional`的方式更加优雅和灵活。同时，`Optional`也可能会带来一些误解。

下面先说一些在我看来的不合适的使用：



## Bad Practice

#### 1. 直接使用 isPresent()进行 if 检查

这个直接参考上面的例子，用`if`判断和 1.8 之前的写法并没有什么区别，反而返回值包了一层`Optional`，增加了代码的复杂性，没有带来任何实质的收益。其实`isPresent()`一般用于流处理的结尾，用于判断是否符合条件。

```
list.stream()    .filer(x -> Objects.equals(x,param))    .findFirst()    .isPresent()
```

复制代码

#### 2. 在方法参数中使用 Optional

我们用一个东西之前得想明白，这东西是为解决什么问题而诞生的。`Optional`直白一点说就是为了表达可空性，如果方法参数可以为空，为何不重载呢？包括使用构造函数也一样。重载的业务表达更加清晰直观。

```
//don't write method like thispublic void getUser(long uid,Optional<Type> userType);
//use Overloadpublic void getUser(long uid) {    getUser(uid,null);}public void getUser(long uid,UserType userType) {    //doing something}
```

复制代码

#### 3. 直接使用 Optional.get

`Optional`不会帮你做任何的空判断或者异常处理，如果直接在代码中使用`Optional.get()`和不做任何空判断一样，十分危险。这种可能会出现在那种所谓的着急上线，着急交付，对`Optional`也不是很熟悉，直接就用了。这里多说一句，可能有人会反问了：甲方/业务着急，需求又多，哪有时间给他去做优化啊？因为我在现实工作中遇到过，但这两者并不矛盾，因为代码行数上差别并不大，只要自己平时保持学习，都是信手拈来的东西。

#### 4. 使用在 POJO 中

估计很少有人这么用：

```
public class User {    private int age;    private String name;    private Optional<String> address;}
```

复制代码

这样的写法将会给序列化带来麻烦，`Optional`本身并没有实现序列化，现有的 JSON 序列化框架也没有对此提供支持的。

#### 5. 使用在注入的属性中

这种写法估计用的人会更少，但不排除有脑洞的。

```
public class CommonService {    private Optional<UserService> userService;        public User getUser(String name) {        return userService.ifPresent(u -> u.findByName(name));    }}
```

复制代码

首先依赖注入大多在 spring 的框架之下，直接使用`@Autowired`很方便。但如果使用以上的写法，如果`userService` set 失败了，程序就应该终止并报异常，并不是无声无息，让其看起来什么问题都没有。

## Best and Pragmatic Practice

### API

在说最佳实践前，让我们来看一下`Optional`都提供了哪些常用 API。

#### 1. [empty](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#empty--)()

返回一个`Optional`容器对象，而不是 null。**建议常用⭐⭐⭐⭐**

#### 2. [of](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#of-T-)(T value)

创建一个`Optional`对象，如果 value 是 null，则抛出 NPE。**不建议用⭐⭐**

#### 3. [ofNullable](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#ofNullable-T-)(T value)

同上，创建一个`Optional`对象，但 value 为空时返回`Optional.empty()`。**推荐使用⭐⭐⭐⭐⭐**

#### 4. [get()](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#get--) 

返回`Optional`中包装的值，在判空之前，千万不要直接使用！**尽量别用！⭐**

#### 5. [orElse](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#orElse-T-)([T](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) other)

同样是返回`Optional`中包装的值，但不同的是当取不到值时，返回你指定的 default。**看似很好，但不建议用⭐⭐**

#### 6. [orElseGet](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#orElseGet-java.util.function.Supplier-)([Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)<? extends [T](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html)> other)

同样是返回`Optional`中包装的值，取不到值时，返回你指定的 default。**看似和 5 一样，但推荐使用⭐⭐⭐⭐⭐**

#### 7. [orElseThrow](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#orElseThrow-java.util.function.Supplier-)([Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)<? extends X> exceptionSupplier)

返回`Optional`中包装的值，取不到值时抛出指定的异常。**阻塞性业务场景推荐使用⭐⭐⭐⭐**

#### 8. [isPresent](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#isPresent--)()

判断`Optional`中是否有值，返回 boolean，某些情况下很有用，但尽量不要用在 if 判断体中。**可以用⭐⭐⭐**

#### 9. [ifPresent](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#ifPresent-java.util.function.Consumer-)([Consumer](https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html)<? super [T](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html)> consumer)

判断`Optional`中是否有值，有值则执行 consumer，否则什么都不干。**日常情况下请使用这个⭐⭐⭐⭐**

### TIPS

首先是一些基本原则：

- 不要声明任何`Optional`实例属性
- 不要在任何 setter 或者构造方法中使用`Optional`
- `Optional`属于返回类型，在业务返回值或者远程调用中使用

#### 1. 业务上需要空值时，不要直接返回 null，使用`**Optional.empty()**` 

```
public Optional<User> getUser(String name) {    if (StringUtil.isNotEmpty(name)) {        return RemoteService.getUser(name);    }     return Optional.empty();}
```

复制代码

#### 2. 使用 orElseGet()

获取 value 有三种方式：`get()` `orElse()` `orElseGet()`。这里推荐在需要用到的地方只用 `orElseGet()`。

首先，`get()`不能直接使用，需要结合判空使用。这和`!=null`其实没多大区别，只是在表达和抽象上有所改善。

其次，为什么不推荐`orElse()`呢？因为`orElse()`无论如何都会执行括号中的内容， `orElseGet()`只在主体 value 是空时执行，下面看个例子：

```java
public String getName() {    System.out.print("method called");}
String name1 = Optional.of("String").orElse(getName()); //output: method calledString name2 = Optional.of("String").orElseGet(() -> getName()); //output:
```

复制代码

如果上面的例子`getName()`方法是一个远程调用，或者涉及大量的文件 IO，代价可想而知。

但 `orElse()`就一无是处吗？并不是。`orElseGet()`需要构建一个`Supplier`，如果只是简单的返回一个静态资源、字符串等等，直接返回**静态**资源即可。

```java
public static final String USER_STATUS = "UNKNOWN";...public String findUserStatus(long id) {    Optional<String> status = ... ; //     return status.orElse(USER_STATUS);}
//不要这么写public String findUserStatus(long id) {    Optional<String> status = ... ; //     return status.orElse("UNKNOWN");//这样每次都会新建一个String对象}
```

复制代码

#### 3. 使用 orElseThrow()

这个针对阻塞性的业务场景比较合适，例如没有从上游获取到用户信息，下面的所有操作都无法进行，那此时就应该抛出异常。正常的写法是先判空，再手动 throw 异常，现在可以集成为一行：

```java
public String findUser(long id) {    Optional<User> user = remoteService.getUserById(id) ;    return user.orElseThrow(IllegalStateException::new);}
```

复制代码

#### 4. 不为空则执行时，使用 ifPresent()

这点没有性能上的优势，但可以使代码更简洁：

```java
//之前是这样的if (status.isPresent()) {    System.out.println("Status: " + status.get());}
//现在status.ifPresent(System.out::println);
```

复制代码

#### 5. 不要滥用

有些简单明了的方法，完全没必要增加`Optional`来增加复杂性。

```java
public String fetchStatus() {    String status = getStatus() ;    return Optional.ofNullable(status).orElse("PENDING");}
//判断一个简单的状态而已public String fetchStatus() {    String status = ... ;    return status == null ? "PENDING" : status;}
```

复制代码



首先，null 可以作为集合的元素之一，它并不是非法的；其次，集合类型本身已经具备了完整的空表达，再去包装一层`Optional`也是徒增复杂，收益甚微。例如，map 已经有了`getOrDefault()`这样的类似`orElse()`的 API 了。

## 总结

`Optional`的出现使 Java 对 null 的表达能力更近了一步，好马配好鞍，合理使用可以避免大量的 NPE，节省大量的人力物力。以上内容也是本人查询了很多资料，边学边写的产出，如有错漏之处，还请不吝指教。