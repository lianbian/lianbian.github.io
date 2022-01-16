---
title: git暂存操作
date: 2021-06-08
categories: 软件工具
tags: git
---

## 使用场景

在正常的业务开发过程中，突然有一个线上紧急bug需要修复，这个时候需要切换分支去修复bug，当前的代码由于是一个半成品，所以不能进行代码的add commit push



## git暂存

```git
git add . 指定文件
# git stash 暂存（存储在本地，并将项目本次操作还原）
git stash save "注释"
git stash -a 或 git stash save -a "注释"  有新文件的时候，使用 -a 参数
git stash pop 使用上一次暂存，并将这个暂存删除（使用该命令后，如果有冲突，终端会显示，如果有冲突，先解决冲突）
git stash apply stash@{id} stash@{id}里面的id默认从0开始，最近的暂存为0
git stash list 查看所有的暂存
git stash clear 清空所有的暂存
git stash drop [-q|--quiet] [<stash>] 删除某一个暂存，在中括号里边放置需要删除的暂存ID
git stash apply 使用某个暂存，但是不会删除这个暂存
```



## 暂存不小心清空，结果里面有需要的代码，找回方法

```git
git fsck --lost-found
git show
git merge
```

### 举例

```git
# 查看记录
git fsck --lost-found
```

![image-20210608193841918](http://mkstatic.lianbian.net/image-20210608193841918.png)

```git
# 查看对比
git show d1991029610a6a48e2782f91798aec2f22bd39aa
# 合并
git merge d1991029610a6a48e2782f91798aec2f22bd39aa
```

