package wtf.s1.android.thread

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ThreadLogImp: ThreadLog {

    private val threadSet: MutableSet<S1Thread> = Collections.newSetFromMap(ConcurrentHashMap())

    private val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    override fun onThreadNew(t: Thread) {
        val newThread = S1Thread(t)
        threadSet.add(newThread)
        listeners.forEach {
            it.onThreadCreate(newThread)
        }
    }

    override fun onThreadRun(t: Thread) {
        val newThread = S1Thread(t)
        threadSet.add(newThread)
        listeners.forEach {
            it.onThreadRun(newThread)
        }
    }

    override fun addOnThreadCreateListener(listener: OnThreadCreateListener) {
        listeners.add(listener)
    }

    override fun removeOnThreadCreateListener(listener: OnThreadCreateListener) {
        listeners.remove(listener)
    }

    override fun getAllThread(): Collection<S1Thread> {
        return threadSet
    }

}