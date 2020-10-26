package wtf.s1.android.thread

object ThreadInspector {

    var log: ThreadLog? = ThreadLogImp()

    fun threadNew(thread: Thread, stackTraceElements: Array<StackTraceElement>?) {
        log?.onThreadNew(thread, stackTraceElements)
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