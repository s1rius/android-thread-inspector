package wtf.s1.android.thread

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ThreadLogImp: ThreadLog {

    private val threadMap = ConcurrentHashMap<Long, S1Thread>()
    private val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    override fun getThread(tid: Long): S1Thread? {
        return threadMap[tid]
    }

    override fun onThreadNew(t: S1Thread) {
        val newT = threadMap[t.id]?.update(t) ?: t
        threadMap[newT.id] = newT
        listeners.forEach {
            it.onThreadCreate(newT)
        }
    }

    override fun onThreadUpdate(t: S1Thread) {
        val newT = threadMap[t.id]?.update(t) ?: t
        threadMap[t.id] = newT
        listeners.forEach {
            it.onThreadUpdate(newT)
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