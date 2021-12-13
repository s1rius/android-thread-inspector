package wtf.s1.android.thread.flipper

import android.os.Handler
import android.os.Looper
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperPlugin
import wtf.s1.android.thread.OnThreadCreateListener
import wtf.s1.android.thread.S1Thread
import wtf.s1.android.thread.ThreadInspector

class S1ThreadPlugin : FlipperPlugin, OnThreadCreateListener {

    companion object {
        const val TAG = "s1ThreadPlugin"
        const val NEW_THREAD = "newThread"
        const val UPDATE_THREAD = "updateThread"
    }

    init {
        ThreadInspector.getThreadLog()?.addOnThreadCreateListener(this)
    }

    private var connection: FlipperConnection? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onConnect(connection: FlipperConnection?) {
        this.connection = connection
        ThreadInspector.getAllThread()?.forEach {
            newRow(it)
        }
    }

    override fun runInBackground(): Boolean = true


    override fun getId(): String = "s1-thread-inspector"


    override fun onDisconnect() {
        connection = null
    }

    override fun onThreadCreate(
        thread: S1Thread
    ) {
        handler.post{ newRow(thread) }
    }

    override fun onThreadUpdate(thread: S1Thread) {
        handler.post { updateRow(thread) }

    }

    fun newRow(thread: S1Thread) {
        connection?.let {
            it.send(NEW_THREAD, FlipperMessage(thread).toFlipperObject())
        }
    }

    fun updateRow(thread: S1Thread) {
        connection?.let {
            it.send(UPDATE_THREAD, FlipperMessage(thread).toFlipperObject())
        }
    }
}