package wtf.s1.buildsrc

object Versions{
    const val applicationId = "wtf.s1.aaa"
    const val kotlin = "1.5.10"
    const val ktx = "1.0.0"
    const val coroutines = "1.5.0"
    const val gradlePlugin ="7.0.0"
    const val lifecycle = "2.2.0"
    const val compileSdkVersion = 31
    const val minSdkVersion = 16
    const val targetSdkVersion = 31
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object Plugins{
    const val androidLib = "com.android.library"
}

object Deps{
    const val remoteThreadCore = "wtf.s1.pudge:thread-inspector-core:1.1.0"
    const val remoteThreadFlipper = "wtf.s1.pudge:thread-inspector-flipper:1.1.0"
    const val remoteThreadBhook = "wtf.s1.pudge:thread-inspector-bhook:1.1.0"

    object Kotlin {
        const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val coroutines =
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val coroutinesAndroid =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        const val ktxCore = "androidx.core:core-ktx:${Versions.ktx}"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.1.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
        const val extension = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
        const val livedata = "androidx.lifecycle:lifecycle-livedata:${Versions.lifecycle}"
        const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
        const val annotationx = "androidx.annotation:annotation:1.3.0"
        object Core {
            const val utils = "androidx.legacy:legacy-support-core-utils:1.0.0"
        }
    }

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.0"

    const val flipper = "com.facebook.flipper:flipper:0.123.0"
    const val flipperNoOp = "com.facebook.flipper:flipper-noop:0.123.0"

    const val soloader = "com.facebook.soloader:soloader:0.9.0"

    const val epic = "com.github.tiann:epic:0.11.2"
    const val bhook = "com.bytedance:bytehook:1.0.3"
}

object ClassPaths {
    const val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePlugin}"
    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val mavenPlugin = "com.vanniktech:gradle-maven-publish-plugin:0.17.0"
    const val dokaa = "org.jetbrains.dokka:dokka-gradle-plugin:1.4.32"
}