---
title: 解决Maven同一依赖多版本共存
keywords: java Maven
categories: Maven
author: 连边
date: 2021-10-28
tags:
  - java
---



## 问题现状

由于是一个迭代比较久的项目，项目中已经存在poi-tl 1.5.x的依赖，poi-tl v1.5.x是构建在Apache poi3.16上的版本，而我们现在要使用到easyexcel来处理导出，而easyexcel最低的Apache poi版本要求是4.1.2，将项目中已有poi的3.16升级到4.12时，旧代码出错，但是不升级就无法使用easyexcel。



## 解决思路

解决问题思路无非就是两种：



1. 将项目中已有的poi3.16升级到4.12，解决升级后代码出错的地方；

   优点：jar包依赖清晰，在代码改动量可控的情况下，推荐使用该方式；

   缺点：代码改动量大，工作量大，而且容易对之前的功能带来不稳定因素；

   

2. 使用maven-shade-plugin插件来解决，让项目依赖多个版本的poi版本；

   优点：对原来的功能无影响，代码改动量小；

   缺点：jar依赖变大，因为依赖了同一个依赖的2个版本；jar包依赖不那么清晰优雅；



今天文章主角就是第2种方式。

**它的核心思路就是把easyexcel中的高版本poi包改个名字，同时easyexcel中引用的地方也改名（自动），并且代码中用高本版的地方也改个名（手动）。**



## 解决问题

1. 创建一个空maven项目，项目名称为jarjar，引入easyexcel的依赖；

   ```xml
   <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>easyexcel</artifactId>
       <version>3.1.1</version>
   </dependency>
   ```

   查看其依赖的poi版本

   ![easyexcel poi版本](https://mkstatic.lianbian.net/202208160749041.png)

2. 引入插件并且配置好修改的方式；

   ```xml
   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-shade-plugin</artifactId>
               <version>3.2.4</version>
               <executions>
                   <execution>
                       <phase>package</phase>
                       <goals>
                           <goal>shade</goal>
                       </goals>
                       <configuration>
                           <createDependencyReducedPom>true</createDependencyReducedPom>
                           <relocations>
                               <relocation>
                                   <!-- <build>
           <plugins>
               <plugin>
                   <groupId>org.apache.maven.plugins</groupId>
                   <artifactId>maven-shade-plugin</artifactId>
                   <version>3.2.4</version>
                   <executions>
                       <execution>
                           <phase>package</phase>
                           <goals>
                               <goal>shade</goal>
                           </goals>
                           <configuration>
                               <createDependencyReducedPom>true</createDependencyReducedPom>
                               <relocations>
                                   <relocation>
                                       <!-- 改名前 -->
                                       <pattern>org.apache.poi</pattern>
                                       <!-- 改名后 -->
                                       <shadedPattern>shaded.org.apache.poi</shadedPattern>
                                   </relocation>
   
                                   <!-- 可以配置多个 -->
                                   <relocation>
                                       <!-- 改名前 -->
                                       <pattern>com.deepoove.poi.XWPFTemplate</pattern>
                                       <!-- 改名后 -->
                                       <shadedPattern>shaded.com.deepoove.poi.XWPFTemplate</shadedPattern>
                                   </relocation>
                               </relocations>
                           </configuration>
                       </execution>
                   </executions>
               </plugin>
           </plugins>
       </build>改名前 -->
                                   <pattern>org.apache.poi</pattern>
                                   <!-- 改名后 -->
                                   <shadedPattern>shaded.org.apache.poi</shadedPattern>
                               </relocation>
                           </relocations>
                       </configuration>
                   </execution>
               </executions>
           </plugin>
       </plugins>
   </build>
   ```

   较为完整的pom.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
   
       <groupId>org.example</groupId>
       <artifactId>jarjar</artifactId>
       <version>1.0-SNAPSHOT</version>
   
       <dependencies>
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>easyexcel</artifactId>
               <version>3.1.1</version>
           </dependency>
       </dependencies>
   
       <properties>
           <maven.compiler.source>8</maven.compiler.source>
           <maven.compiler.target>8</maven.compiler.target>
       </properties>
   
       <build>
           <plugins>
               <plugin>
                   <groupId>org.apache.maven.plugins</groupId>
                   <artifactId>maven-shade-plugin</artifactId>
                   <version>3.2.4</version>
                   <executions>
                       <execution>
                           <phase>package</phase>
                           <goals>
                               <goal>shade</goal>
                           </goals>
                           <configuration>
                               <createDependencyReducedPom>true</createDependencyReducedPom>
                               <relocations>
                                   <relocation>
                                       <!-- 改名前 -->
                                       <pattern>org.apache.poi</pattern>
                                       <!-- 改名后 -->
                                       <shadedPattern>shaded.org.apache.poi</shadedPattern>
                                   </relocation>
   
                                   <!-- 可以配置多个 -->
                                   <relocation>
                                       <!-- 改名前 -->
                                       <pattern>com.deepoove.poi.XWPFTemplate</pattern>
                                       <!-- 改名后 -->
                                       <shadedPattern>shaded.com.deepoove.poi.XWPFTemplate</shadedPattern>
                                   </relocation>
                               </relocations>
                           </configuration>
                       </execution>
                   </executions>
               </plugin>
           </plugins>
       </build>
   
   </project>
   ```

   

3. 打出jar包；

   执行`mvn package`，如果是IDEA直接双击Lifecycle中的`package`就行了。

   ![打出jar包](https://mkstatic.lianbian.net/202208160738987.png)

   这时target目录中会有两个包，一个是original开头的原本包，因为我们没有新建类，所以这个包是空的。
   另一个是和`artifactId-version.jar`的包，`artifactId`和`version`是本项目创建时填写的坐标。

   如图，我的这个maven项目叫jarjar，版本是1.0：

   ![找到指定的jar包](https://mkstatic.lianbian.net/202208160751564.png)

4. 依赖本地jar，运行项目；

   1. 自定义目录，这里在project的根目录新建lib文件夹，将jar放进去

   2. 引入jar包

      ![jarjar项目的pom信息](https://mkstatic.lianbian.net/202208160759463.png)

   ```xml
   <dependency>
               <groupId>org.example</groupId>
               <artifactId>jarjar</artifactId>
               <version>1.0-SNAPSHOT</version>
               <scope>system</scope>
               <systemPath>${project.basedir}/src/main/java/lib/jarjar-1.0-SNAPSHOT.jar</systemPath>
   </dependency>
   ```

   3. 处理打包

   ```xml
    <build>
      <resources>
       <resource>
         <directory>lib</directory>
         <targetPath>/BOOT-INF/lib/</targetPath>
         <includes>
           <include>**/*.jar</include>
         </includes>
       </resource>
      </resources>
    </build>
   ```

   

**至此，已经无损的引入了easyexcel依赖，easyexcel中引用的地方也改名（自动），并且代码中用高本版的地方也改个名（手动）。**



## 相关阅读

插件官网：https://maven.apache.org/plugins/maven-shade-plugin/index.html

maven-shade-plugin解决Maven同一依赖多版本共存：https://www.cnblogs.com/lixin-link/p/15507326.html

maven引入本地jar包的方法：https://cloud.tencent.com/developer/article/1510883



