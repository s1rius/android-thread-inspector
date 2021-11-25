package wtf.s1.android.thread

import java.lang.StringBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ThreadLogImp: ThreadLog {

    private val threadMap = ConcurrentHashMap<Long, S1Thread>()
    private val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    override fun getThread(tid: Long): S1Thread? {
        return threadMap.get(tid)
    }

    override fun onThreadNew(t: S1Thread) {
        threadMap[t.id] = t
        listeners.forEach {
            it.onThreadCreate(t)
        }
    }

    override fun onThreadUpdate(t: S1Thread) {
        threadMap[t.id] = t
        listeners.forEach {
            it.onThreadRun(t)
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