package wtf.s1.android.thread

interface ThreadLog {

    fun onThreadNew(t: S1Thread)

    fun onThreadUpdate(t: S1Thread)

    fun addOnThreadCreateListener(listener: OnThreadCreateListener)

    fun removeOnThreadCreateListener(listener: OnThreadCreateListener)

    fun getAllThread(): Collection<S1Thread>

    fun getThread(tid: Long): S1Thread?
}