package wtf.s1.android.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import wtf.s1.android.thread.jar.ThreadFromJar
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "thread-inspector"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.new_thread).setOnClickListener {
            object: Thread("new thread"){
                override fun run() {
                    super.run()
                    Log.i(TAG, "new thread run")
                }
            }.start()
        }

        findViewById<View>(R.id.new_thread_from_jar).setOnClickListener {
            ThreadFromJar.run()
        }

        findViewById<View>(R.id.new_executor).setOnClickListener {
            Executors.newCachedThreadPool().submit {
                Log.i(TAG, "thread pool submit")
            }
        }

        lifecycleScope.launch {
            async(Dispatchers.IO) {
                Log.i(TAG, "coroutine")
            }
        }
    }
}
