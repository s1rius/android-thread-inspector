package wtf.s1.android.sample

import android.util.Log
import android.app.Application
import android.content.Context
import com.bytedance.android.bytehook.ByteHook
import wtf.s1.android.sample.BuildConfig.*
import wtf.s1.android.thread.bhook.S1ThreadHooker

open class App : Application() {

    override fun attachBaseContext(base: Context?) {
        ByteHook.init(
            ByteHook.ConfigBuilder()
                .setMode(ByteHook.Mode.AUTOMATIC)
                .setDebug(DEBUG)
                .build()
        )
        S1ThreadHooker.hookThread()
        super.attachBaseContext(base)
    }
}