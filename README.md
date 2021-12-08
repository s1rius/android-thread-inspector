# android-thread-inspector

一个简单的 [Flipper](https://github.com/facebook/flipper) 插件，简单集成后可以在 Flipper 的桌面程序上查看线程的创建情况，包括线程创建的调用堆栈。

![preview](https://github.com/s1rius/android-thread-inspector/blob/master/art/1.png)

# 集成

使用 [buildType](https://developer.android.com/studio/build/build-variants#build-types) 仅在 Debug 模式下集成该插件，用于调试。


添加依赖

```groovy
debugImplementation "wtf.s1.pudge:thread-inspector-flipper:x.x.x"
debugImplementation "wtf.s1.pudge:thread-inspector-bhook:x.x.x"

```

在 src/debug/DebugApp.kt 中初始化

```kotlin
class DebugApp: App() {

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(S1ThreadPlugin())
            client.start()
        }
    }

    override fun attachBaseContext(base: Context?) {
        ByteHook.init(
            ByteHook.ConfigBuilder()
                .setMode(ByteHook.Mode.AUTOMATIC)
                .setDebug(BuildConfig.DEBUG)
                .build()
        )
        S1ThreadHooker.hookThread()
        super.attachBaseContext(base)
    }
}
```

## 原理

使用了 [bhook](https://github.com/bytedance/bhook) 对`pthread_create`和`pthread_setname_np`函数进行 hook。