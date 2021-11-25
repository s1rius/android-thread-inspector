package wtf.s1.android.sample

import android.util.Log
import android.app.Application
import android.content.Context
import com.bytedance.android.bytehook.ByteHook
import wtf.s1.android.thread.bhook.S1ThreadHooker

open class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        S1ThreadHooker.hookThread(1)
    }
}