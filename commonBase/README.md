# CommonBase 基础组件库

## 📖 模块概述

CommonBase 是 Android 应用的基础组件库，提供了完整的开发基础设施，包括 UI 基类、工具类集合、扩展函数、事件总线等核心功能。

## 📦 依赖说明

```kotlin
// 在 app/build.gradle.kts 中添加依赖
implementation(project(":commonBase"))
```

## 🏗️ 使用方式

### 1. 初始化应用

```kotlin
// 在 Application 中继承 BaseApplication
class MyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        // 其他初始化代码
    }
}
```

### 2. 使用基类

#### Activity 基类
```kotlin
@RegisterEventBus // 可选：自动注册 EventBus
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    
    override val mViewModel: MainViewModel by viewModels()
    
    override fun createVB(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 初始化 UI
    }
    
    override fun initObserve() {
        // 观察 LiveData
        observeLiveData(mViewModel.data) { data ->
            // 处理数据变化
        }
    }
    
    override fun initData() {
        // 加载数据
    }
}
```

#### Fragment 基类
```kotlin
@RegisterEventBus // 可选：自动注册 EventBus
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    
    override val mViewModel: HomeViewModel by viewModels()
    
    override fun createVB(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 初始化 UI
    }
    
    override fun initObserve() {
        // 观察 LiveData
    }
    
    override fun initData() {
        // 加载数据
    }
}
```

### 3. 使用工具类

#### 存储工具 (SpUtils)
```kotlin
// 存储数据
SpUtils.putString("key", "value")
SpUtils.putInt("age", 25)
SpUtils.putBoolean("isLogin", true)

// 读取数据
val name = SpUtils.getString("key", "")
val age = SpUtils.getInt("age", 0)
val isLogin = SpUtils.getBoolean("isLogin", false)

// 支持任意类型
SpUtils.put("user", userObj)
```

#### Toast 工具
```kotlin
// 简单使用
toast("Hello World")
toast(R.string.message)

// 指定时长
toast("Long message", Toast.LENGTH_LONG)
```

#### 剪贴板工具
```kotlin
// 复制到剪贴板
ClipboardUtils.copyToClipboard("复制的内容", "标签")

// 注意：读取剪贴板需要权限
```

#### 权限管理
```kotlin
// 请求权限
PermissionUtil().requestPermission(this) { allGranted, deniedList ->
    if (allGranted) {
        // 所有权限已授予
    } else {
        // 有权限被拒绝
    }
}
```

### 4. 使用扩展函数

#### View 扩展
```kotlin
// 设置可见性
view.visible()
view.gone()
view.invisible()

// 设置宽度高度
view.width(100)
view.height(200)
view.widthAndHeight(100, 200)

// 带动画的宽高变化
view.animateWidth(200, 300) { progress ->
    // 动画进度回调
}

// 防抖点击
view.setOnSingleClickListener(1000) { v ->
    // 点击事件，1秒内只触发一次
}
```

#### Activity 扩展
```kotlin
// 设置是否允许截屏
activity.isAllowScreenCapture(false)

// 检查是否处于前台
val isResumed = activity.isResumed()
```

#### 尺寸单位转换
```kotlin
// Context/Fragment 中使用
val px = dp2px(16f)
val dp = px2dp(48f)
val sp = sp2px(14f)
```

#### ViewModel 协程扩展
```kotlin
class MyViewModel : ViewModel() {
    
    fun loadData() {
        // IO 线程执行
        launchIO {
            // 网络请求或数据库操作
        }
        
        // 主线程执行
        launchMain {
            // UI 更新
        }
        
        // 带异常处理
        launchIO(exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            // 异常处理
        }) {
            // 可能抛出异常的操作
        }
    }
}
```

### 5. 使用 EventBus

#### 注册事件
```kotlin
// 在类上添加注解
@RegisterEventBus
class MyActivity : BaseActivity<*, *>() {
    
    // 订阅事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        // 处理事件
    }
}
```

#### 发送事件
```kotlin
// 发送普通事件
EventBusUtil.post(MessageEvent("Hello"))

// 发送粘性事件
EventBusUtil.postSticky(StickyEvent("Data"))

// 移除粘性事件
EventBusUtil.removeStickyEvent(StickyEvent::class.java)
```

### 6. 前后台监听

```kotlin
class MyApplication : BaseApplication(), ForegroundBackgroundObserver {
    
    override fun onCreate() {
        super.onCreate()
        ForegroundBackgroundHelper.addObserve(this)
    }
    
    override fun foregroundBackgroundNotify(isForeground: Boolean) {
        if (isForeground) {
            // 应用进入前台
        } else {
            // 应用进入后台
        }
    }
}
```

### 7. 状态栏和导航栏工具

```kotlin
// 设置状态栏颜色
BarUtils.setStatusBarColor(this, Color.BLUE)

// 设置状态栏为浅色模式
BarUtils.setStatusBarLightMode(this, true)

// 隐藏状态栏
BarUtils.setStatusBarVisibility(this, false)

// 获取状态栏高度
val statusBarHeight = BarUtils.getStatusBarHeight()

// 设置导航栏颜色（API 21+）
BarUtils.setNavBarColor(this, Color.BLACK)

// 判断是否支持导航栏
val hasNavBar = BarUtils.isSupportNavBar()
```

### 8. 日期工具

```kotlin
// 时间戳转格式化字符串
val dateStr = DateUtils.getDateFormatString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")

// 字符串转时间戳
val timestamp = DateUtils.getDateStringToDate("2024-01-01", "yyyy-MM-dd")

// 计算时间差
val gapTime = DateUtils.getGapTime(3665000) // 返回 "1:1:5"

// 获取日期区间
val dates = DateUtils.getExcerptDate(false, 0, 7, "yyyy-MM-dd") // 获取最近7天
```

### 9. Activity 栈管理

```kotlin
// 获取当前 Activity
val current = ActivityStackManager.getCurrentActivity()

// 结束指定 Activity
ActivityStackManager.finishActivity(MainActivity::class.java)

// 返回到指定 Activity
ActivityStackManager.backToSpecifyActivity(HomeActivity::class.java)

// 结束其他所有 Activity
ActivityStackManager.popOtherActivity()
```

### 10. 图片加载

```kotlin
// 基础用法
imageView.load("https://example.com/image.jpg")

// 使用 GIF 加载器
val gifLoader = CoilGIFImageLoader.imageLoader
imageView.load("https://example.com/animation.gif", imageLoader = gifLoader)

// 带占位图
imageView.load(url) {
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    crossfade(true)
}
```

## 🔧 配置说明

### ProGuard 配置

库已包含基本的混淆规则，如果遇到问题可添加：

```proguard
# EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# MMKV
-keep class com.tencent.mmkv.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
```

## 🚨 注意事项

1. **BaseApplication** 必须在 AndroidManifest.xml 中注册
2. **EventBus** 使用时需要添加 `@RegisterEventBus` 注解
3. **权限申请** 需要在 AndroidManifest.xml 中声明相应权限
4. **混淆配置** 确保添加必要的 keep 规则
5. **生命周期** 避免在 Activity/Fragment 销毁后执行耗时操作

## 📱 版本要求

- **最低 SDK**: 24 (Android 7.0)
- **编译 SDK**: 36 (Android 14)
- **Kotlin**: 1.9+
- **Java**: 17

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个模块。

## 📄 许可证

本项目采用 MIT 许可证。