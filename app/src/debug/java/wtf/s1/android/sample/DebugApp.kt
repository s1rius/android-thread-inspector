package wtf.s1.android.sample

import android.content.Context
import com.bytedance.android.bytehook.ByteHook
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.soloader.SoLoader
import wtf.s1.android.thread.bhook.S1ThreadHooker
import wtf.s1.android.thread.flipper.S1ThreadPlugin

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