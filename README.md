# android-thread-inspector

一个追踪 java 及 native 线程创建的 [Flipper](https://github.com/facebook/flipper) 插件

![preview](https://github.com/s1rius/android-thread-inspector/blob/master/art/1.png)

# 集成

建议仅在仅在 Debug [buildType](https://developer.android.com/studio/build/build-variants#build-types) 下集成该插件，用于调试。


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


## 已知问题

- java 线程 id 与 linux 线程 id 不一致，导致 hook 之前获取到的 java 线程会重复显示
- 1.x 版本不提供 native 的堆栈信息

## 协议
MIT

感谢 [bhook](https://github.com/bytedance/bhook) 及 [koom](https://github.com/KwaiAppTeam/KOOM) 的贡献者