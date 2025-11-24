# TikTok关注列表Demo

## 项目介绍
本项目是一个模拟抖音关注列表功能的Android原生应用，基于Java语言开发，适配Android 7.0+（API 24），实现了关注管理、状态切换、本地数据持久化等核心功能，还原了短视频平台关注列表的经典交互逻辑。

## 技术栈
- 开发语言：Java
- 开发工具：Android Studio
- 最低兼容版本：Android 7.0（API 24）
- Gradle版本：8.13
- 核心框架/组件：
  - AndroidX（AppCompat、ConstraintLayout、RecyclerView）
  - Material Design（TabLayout、BottomSheetDialog、SwitchCompat）
  - SwipeRefreshLayout（下拉刷新）
  - SharedPreferences（本地数据存储）
  - ViewBinding（视图绑定，简化布局操作）

## 功能特点
### 1. 栏目切换
- 顶部支持「互关」「关注」「粉丝」「朋友」四个滑动栏目
- 默认打开APP先进入「互关」栏目
- 「互关」「粉丝」「朋友」栏目：显示专属统计栏（如「我的互关(0人)」）+ 中央「暂无数据」提示
- 「关注」栏目：显示统计栏（「我的关注(X人)」）+ 关注用户列表

### 2. 关注管理
- 关注用户默认显示「已关注」灰色按钮，点击弹出「取消关注」确认框
- 取消关注后：按钮变为红色「关注」，用户不立即消失, 直至刷新后删除
- 重新关注：点击红色「关注」按钮，直接变为灰色「已关注」
- 下拉刷新：彻底删除所有「未关注」用户
- 
- 特别关注：通过底部弹窗的滑动开关设置，设置后在用户名后方显示「特别关注」标识
- 备注设置：支持为用户添加/修改备注，列表优先显示备注
- 本地持久化：所有操作（关注状态、特别关注、备注）实时保存到本地，重启APP/切换栏目不丢失
  持久化采用SharedPreferences, 虽然使用简单, 但仅限于轻量化存储, 鉴于本人技术所限暂用(*/_\*), 理论上使用SQLite和ROOM更好

## 项目结构
```
com.example.tiktokfollow/
├── model/                  // 数据模型
│   └── User.java           // 用户实体类（存储用户信息、关注状态等）
├── adapter/                // 列表适配器
│   └── FollowListAdapter.java  // 关注列表适配（处理UI渲染、点击事件）
├── util/                   // 工具类
│   └── SharedPreferencesUtil.java  // 本地存储工具（保存/读取用户数据）
├── ui/                     // UI页面
│   └── FollowActivity.java // 主页面（核心逻辑：栏目切换、刷新、弹窗交互）
├── res/
│   ├── drawable/           // 图片资源（头像、功能图标）
│   ├── layout/             // 布局文件（主页面、列表项、弹窗）
│   └── values/             // 配置文件（颜色、字符串、样式）
```

## 安装与运行
1. 环境要求：Android Studio Hedgehog及以上版本，Gradle 8.13兼容
2. 配置图片资源：将头像图片（avatar1.png, avatar2.png...）、功能图标（ic_more.png）放入 `res/drawable/` 目录
3. 运行项目：连接Android设备或启动模拟器（Android 7.0+），点击运行按钮

## 核心逻辑说明
1. **数据流转**：用户操作（关注/取消关注/备注）→ 更新内存列表 → 同步保存到SharedPreferences → 刷新时读取最新数据
2. **栏目切换**：通过ViewStub延迟加载非关注栏布局，减少初始渲染压力，切换时隐藏其他栏目
3. **下拉刷新**：过滤本地存储中未关注的用户 → 重新保存过滤结果 → 刷新列表UI
4. **状态保持**：取消关注后仅更新状态标记，不立即删除用户；刷新时才彻底删除未关注用户，确保切换栏目不恢复

## 注意事项
1. 图片资源命名需严格匹配（avatar1.png, avatar2.png..., ic_more.png），否则会导致头像/图标显示异常
2. 若需修改关注用户默认数量，修改 `SharedPreferencesUtil.generateDefaultData()` 中的循环次数
3. 本地存储采用SharedPreferences，适合小规模数据（本项目默认150个用户），如需扩展可替换为Room数据库
