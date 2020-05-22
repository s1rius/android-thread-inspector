package wtf.s1.android.thread

import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ThreadLogImp: ThreadLog() {

    val threadSet: MutableSet<S1Thread> = Collections.newSetFromMap(ConcurrentHashMap())

    val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    fun threadNew(t: Thread) {
        Log.i("s1rius", t.name)
        val newThread = S1Thread(t)
        threadSet.add(newThread)
        listeners.forEach {
            it.onThreadCreate(newThread)
        }
    }

    fun addOnThreadCreateListener(listener: OnThreadCreateListener) {
        listeners.add(listener)
    }

    fun removeOnThreadCreateListener(listener: OnThreadCreateListener) {
        listeners.remove(listener)
    }

}