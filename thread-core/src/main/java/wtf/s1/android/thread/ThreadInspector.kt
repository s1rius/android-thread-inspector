package wtf.s1.android.thread

import android.app.ActivityManager
import android.content.Context
import android.os.Process

object ThreadInspector {

    var log: ThreadLog? = ThreadLogImp()

    fun threadNew(thread: Thread) {
        log?.onThreadNew(thread)
    }

    fun threadRun(thread: Thread) {
        log?.onThreadRun(thread)
    }

    fun getAllThread(): Collection<S1Thread>? {
        return log?.getAllThread()
    }

    fun getThreadLog(): ThreadLog? {
        return log
    }
}