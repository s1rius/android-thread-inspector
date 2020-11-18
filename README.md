# android-thread-inspector

一个简单的 [Flipper](https://github.com/facebook/flipper) 插件，简单集成后可以在 Flipper 的桌面程序上查看线程的创建情况，包括线程创建的调用堆栈。

![preview](https://github.com/s1rius/android-thread-inspector/blob/master/art/1.png)

# 集成

使用 [buildType](https://developer.android.com/studio/build/build-variants#build-types) 仅在 Debug 模式下集成该插件，用于调试。


添加依赖

```groovy
debugImplementation "wtf.s1.pudge:thread-inspector-flipper:0.2.0"
```

初始化 Flipper 时添加该插件

```kotlin
client.addPlugin(S1ThreadPlugin())
```

## 原理

使用了 [epic](https://github.com/tiann/epic) 对线程构造函数进行 hook。