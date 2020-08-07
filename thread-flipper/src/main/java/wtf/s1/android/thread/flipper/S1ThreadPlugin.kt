package wtf.s1.android.thread.flipper

import android.util.Log
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperPlugin
import wtf.s1.android.thread.OnThreadCreateListener
import wtf.s1.android.thread.S1Thread
import wtf.s1.android.thread.ThreadInspector
import wtf.s1.android.thread.ThreadLogImp

class S1ThreadPlugin : FlipperPlugin,
    OnThreadCreateListener {

    companion object {
        const val TAG = "s1ThreadPlugin"
        const val NEW_THREAD = "newThread"
        const val UPDATE_THREAD = "updateThread"
    }

    init {
        ThreadInspector.getThreadLog()?.addOnThreadCreateListener(this)
    }

    private var connection: FlipperConnection? = null

    override fun onConnect(connection: FlipperConnection?) {
        Log.i(TAG, "onConnect")
        this.connection = connection
        ThreadInspector.getAllThread()?.forEach {
            newRow(it)
        }
    }

    override fun runInBackground(): Boolean = true


    override fun getId(): String = "s1-thread-inspector"


    override fun onDisconnect() {
        Log.i(TAG, "onDisconnect")
        connection = null
    }

    override fun onThreadCreate(thread: S1Thread) {
        newRow(thread)
    }

    override fun onThreadRun(thread: S1Thread) {
        Log.i(TAG, "thread run $thread")
        updateRow(thread)
    }

    fun newRow(thread: S1Thread) {
        connection?.let {
            Log.i(TAG, "send message")
            it.send(NEW_THREAD, FlipperMessage(thread).toFlipperObject())
        }
    }

    fun updateRow(thread: S1Thread) {
        connection?.let {
            Log.i(TAG, "update message")
            it.send(UPDATE_THREAD, FlipperMessage(thread).toFlipperObject())
        }
    }
}