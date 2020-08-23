package wtf.s1.android.thread

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ThreadLogImp: ThreadLog {

    private val threadMap = ConcurrentHashMap<Long, S1Thread>()
    private val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    override fun onThreadNew(t: Thread) {
        val newThread = S1Thread(t)
        threadMap[newThread.id] = newThread
        listeners.forEach {
            it.onThreadCreate(newThread)
        }
    }

    override fun onThreadRun(t: Thread) {

        var newThread = threadMap[t.id]
        if (newThread == null) {
            newThread = S1Thread(t)
        }
        threadMap[t.id] = newThread
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
        Thread.getAllStackTraces().keys.forEach {
            val s1Thread = threadMap[it.id]
            if (s1Thread == null) {
                threadMap[it.id] = S1Thread(it).apply { this.createTime = 0L }
            } else {
                s1Thread.update(it)
            }
        }
        return threadMap.values
    }

}