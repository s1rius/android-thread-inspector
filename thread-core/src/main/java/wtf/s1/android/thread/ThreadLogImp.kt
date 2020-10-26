package wtf.s1.android.thread

import java.lang.StringBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ThreadLogImp: ThreadLog {

    private val threadMap = ConcurrentHashMap<Long, S1Thread>()
    private val listeners = CopyOnWriteArrayList<OnThreadCreateListener>()

    override fun onThreadNew(
        t: Thread,
        stacktraceArray: Array<StackTraceElement>?
    ) {
        val newThread = S1Thread(t).apply {
            val sb = StringBuilder()
            val array = arrayListOf<String>()
            stacktraceArray?.forEach {
                val item = StringBuilder()
                item.append("Thread new: at ")
                    .append(it.className).append(".")
                    .append(it.methodName)
                    .append("(")
                    .append(it.className.substring(
                        it.className.lastIndexOf(".") + 1))
                    .append(":").append(it.lineNumber)
                    .append(")")

                array.add(item.toString())
                sb.append(item)
            }

            this.stackTraces = array
        }
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