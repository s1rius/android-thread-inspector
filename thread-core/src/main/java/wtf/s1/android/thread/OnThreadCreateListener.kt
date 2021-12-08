package wtf.s1.android.thread

interface OnThreadCreateListener {

    fun onThreadCreate(thread: S1Thread)

    fun onThreadUpdate(thread: S1Thread)
}