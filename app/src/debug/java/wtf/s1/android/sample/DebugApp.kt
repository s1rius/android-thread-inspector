package wtf.s1.android.sample

import android.app.Application
import android.util.Log
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.soloader.SoLoader
import wtf.s1.android.thread.flipper.S1ThreadPlugin

class DebugApp: Application() {

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            Log.i("s1rius", "enable flipper")
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(S1ThreadPlugin())
            client.start()
        }
    }
}