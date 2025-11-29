# TikTok关注列表-模拟服务端数据交互改进

## 项目介绍
本项目模拟抖音关注列表功能的Android应用，基于Java语言开发，适配Android7.0+(API 24)，实现了关注管理、状态切换、本地数据持久化等核心功能
===>补充改进功能, 新增了数据源来自服务端时的数据加载和更新功能，服务端采用分页请求功能, 每次返回10条数据, 通过固定列表大小减少布局计算，
稳定ID提升复用效率，缓存头像资源，使用Glide异步加载头像，使用线程池后台序列化与存储避免阻塞UI等方式提高了流畅度, 减少了卡顿, 满足性能优化要求

## 项目演示
[1,关注列表本地读取-演示视频-BiliBili](https://www.bilibili.com/video/BV1nVUCBVE7z/?spm_id_from=333.1387.homepage.video_card.click&vd_source=bef90db70f95cb50eb617452a4ebf8ce)

[2,服务端交互改进-演示视频-BiliBili](https://www.bilibili.com/video/BV1xqSwBzEZP/?spm_id_from=333.1007.top_right_bar_window_history.content.click&vd_source=bef90db70f95cb50eb617452a4ebf8ce)

---

## 技术栈
- 开发语言：Java
- 开发工具：Android Studio
- 最低兼容版本：Android 7.0（API 24）
- Gradle版本：8.13
- 核心框架/组件：
  - AndroidX（AppCompat、ConstraintLayout、RecyclerView）
  - Material Design（TabLayout、BottomSheetDialog、SwitchCompat）
  - SwipeRefreshLayout
  - SharedPreferences
  - ViewBinding
---

## 功能特点
### 1.栏目切换
- 顶部支持「互关」「关注」「粉丝」「朋友」四个滑动栏目
- 默认打开APP先进入「互关」栏目
- 「互关」「粉丝」「朋友」栏目：显示专属统计栏（如「我的互关(0人)」）+ 中央「暂无数据」提示
- 「关注」栏目：显示统计栏（「我的关注(X人)」）+ 关注用户列表

### 2.关注管理
- 关注用户默认显示「已关注」灰色按钮，点击弹出「取消关注」确认框
- 取消关注后：按钮变为红色「关注」，用户不立即消失, 直至刷新后删除
- 重新关注：点击红色「关注」按钮，直接变为灰色「已关注」
- 下拉刷新：彻底删除所有「未关注」用户
- 点击用户头像弹出toast提示
- 
- 特别关注：通过底部弹窗的滑动开关设置，设置后在用户名后方显示「特别关注」标识
- 备注设置：支持为用户添加/修改备注，列表优先显示备注
- 本地持久化：所有操作（关注状态、特别关注、备注）实时保存到本地，重启APP/切换栏目不丢失
  持久化采用SharedPreferences, 虽然使用简单, 但仅限于轻量化存储, 鉴于技术所限暂用(*/_\*), 理论上使用SQLite和ROOM更好
---

## 改进后项目结构
```
com.example.tiktokfollow/
├── model/
│   └── User.java           // 用户实体类
├── adapter/
│   └── FollowListAdapter.java  // 关注列表适配
├── data/
│   └── FollowRemoteDataSource.java  // 模拟服务器返回数据
├── ui/
│   └── FollowActivity.java // 主页面
├── res/
│   ├── drawable/           // 图片资源
│   ├── layout/             // 布局文件
│   └── values/             // 配置文件
```
---

## 模拟服务端
- 首次启动时：
    - 自动生成**1200**个关注用户, 头像在`avatar1 ~ avatar15`之间循环
    - 保存到`SharedPreferences`，作为服务端数据库
- 之后每次启动从SP读取完整用户列表
- 提供**分页接口**模拟HTTPS API：
  ```java
  public static void getFollowUsers(int page, int pageSize, PageCallback callback);
  ```
- `page`从1开始, `pageSize`由调用方指定[服务端每页固定10条]
- 异步执行，内部使用`ExecutorService`+`Handler`模拟网络请求和延时(默认设为50ms)
- 提供**更新用户接口**：
  ```java
  public static void updateUser(User user, UpdateCallback callback);
  ```
- 可实时更新关注状态、特别关注、备注等, 模拟数据库交互
- 更新后立即写回SP, 相当于调用了服务端更新接口后，服务端持久化保存
---

## 关注页逻辑
### 首次进入关注页
- 在“关注”Tab被选中时, 如果`followList`为空，则调用`loadFirstPage()`从服务端拉取第一页数据
```java
private void showFollowLayout(){}
```

### 下拉刷新
- 通过`SwipeRefreshLayout`监听, `loadFirstPage()`重新加载第一页

### 上拉滚动加载更多
- 为RecyclerView添加`OnScrollListener`监听, 当滚动到底部时，调用`loadNextPage()`加载下一页
```java
private void loadNextPage(){
    if (!hasMore) return;
    requestPage(currentPage + 1, false);
}
```

### 用户操作[取关/关注/备注/特别关注]
- 所有更改用户数据的操作，最终通过`updateUser()`统一封装
- `FollowRemoteDataSource.updateUser`会写入SP相当于更新服务端数据
- 当前列表中对应的item也会同步更新UI
- **刷新/重启启应用后**，仍然能看到最新状态
---

## 性能优化
### 1.RecyclerView设置
在`FollowActivity.initView()`中：
- `setHasFixedSize(true)`：列表大小固定时可优化布局计算
- `setItemViewCacheSize(20)`：多缓存一些item，减少复用切换的开销
- 关闭变更动画：避免频繁`notifyItemChanged`导致的动画卡顿

### 2.头像加载（Glide）
#### 适配器中的使用
- 使用**稳定ID**提升复用效率
- 缓存头像资源ID，避免频繁`getIdentifier`
- 使用Glide加载头像
- 结合RecyclerView优化设置，滚动时能保持较高帧率
---

## 注意事项
1. 图片资源命名需严格匹配（avatar1.png, avatar2.png..., ic_more.png），否则会导致头像/图标显示异常
2. 若需修改服务端初始关注数量,返回每页数据数量和网络延迟时间，请修改`FollowRemoteDataSource.java`中相关参数
