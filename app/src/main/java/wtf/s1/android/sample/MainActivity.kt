package wtf.s1.android.sample

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import wtf.s1.aa.WtfThread
import wtf.s1.android.thread.epic.ThreadHook

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Log.i("ooooo", "oonon")
        }
//
        findViewById<View>(R.id.hook).setOnClickListener {
            val t = object : Thread("unuse ") {
                override fun run() {
                    super.run()
                    print("onono $name")
                }
            }
            t.start()
        }

        /**
        window.decorView.postDelayed({
//            for (i in 1..100) {
            val t = object : Thread("unuse ") {
                override fun run() {
                    super.run()
                    print("onono $name")
                }
            }
            t.start()
//            }
        }, 5000)

         */

//        window.decorView.postDelayed({
//            WtfThread.anotherThread()
//        }, 3000)
//
//        lifecycleScope.launch {
//            async(Dispatchers.IO) {
//                Log.i("ooooo", "coroutines io")
//            }
//        }
    }
}
