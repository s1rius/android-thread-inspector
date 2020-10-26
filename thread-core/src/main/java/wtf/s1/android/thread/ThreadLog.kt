package wtf.s1.android.thread

import androidx.annotation.Keep

@Keep
interface ThreadLog {
    companion object {
        private const val TAG = "ThreadLog"
    }

    fun onThreadNew(t: Thread, stacktraceArray: Array<StackTraceElement>?)

    fun onThreadRun(t: Thread)

    fun addOnThreadCreateListener(listener: OnThreadCreateListener)

    fun removeOnThreadCreateListener(listener: OnThreadCreateListener)

    fun getAllThread(): Collection<S1Thread>
}