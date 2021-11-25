package wtf.s1.android.thread

object ThreadInspector {

    var log: ThreadLog? = ThreadLogImp()

    fun threadCreate(thread: S1Thread) {
        log?.onThreadNew(thread)
    }

    fun threadUpdate(thread: S1Thread) {
        log?.onThreadUpdate(thread)
    }

    fun getThread(tid: Long): S1Thread? {
        return log?.getThread(tid)
    }

    fun getThread(tid: Int): S1Thread? {
        return log?.getThread(tid.toLong())
    }

    fun getAllThread(): Collection<S1Thread>? {
        return log?.getAllThread()
    }

    fun getThreadLog(): ThreadLog? {
        return log
    }
}