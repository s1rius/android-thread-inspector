package wtf.s1.android.thread

interface OnThreadCreateListener {

    fun onThreadCreate(thread: S1Thread)

    fun onThreadRun(thread: S1Thread)
}