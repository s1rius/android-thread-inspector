package wtf.s1.android.thread.bhook

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log

import androidx.annotation.Keep
import wtf.s1.android.thread.S1Thread
import wtf.s1.android.thread.ThreadInspector
import java.util.concurrent.ConcurrentHashMap

@Keep
object S1ThreadHooker {

    const val HOOK_ALL = 0
    const val HOOK_APP = 1

    val stackCache = ConcurrentHashMap<Int, String>()
    val idMap = ConcurrentHashMap<Int, Int>()
    val nameMap = ConcurrentHashMap<Int, String>()

    init {
        System.loadLibrary("s1threadhook")
    }

    @SuppressLint("DefaultLocale")
    @JvmStatic
    fun threadCreate(cid: Int, nativeStack: String?): String {
        val sb: StringBuilder = StringBuilder()
        if (nativeStack != null) {
            sb.append(nativeStack)
            val stackTrace: Array<StackTraceElement> = Thread.currentThread().getStackTrace()
            for (s: StackTraceElement in stackTrace) {
                if (!filterStacktrace(s)) {
                    sb.append("\t at ")
                        .append(s.toString())
                        .append('\n')
                }
            }

            stackCache[cid] = sb.toString();
            if (idMap.contains(cid)) {
                idMap.get(cid)?.let {tid->
                    updateThread(cid, tid)
                }
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun threadStart(tid: Int, cid: Int) {
        idMap[cid] = tid;
        updateThread(cid, tid)
    }

    private fun updateThread(cid: Int, tid: Int) {

        var s1thread = ThreadInspector.getThread(tid);

        if (s1thread == null) {
            s1thread = S1Thread(cid.toLong(), tid.toLong())
            val name: String? = nameMap.remove(tid)
            s1thread.name = name
            ThreadInspector.threadCreate(s1thread)
        }

        var needUpdate = false
        if (stackCache.containsKey(cid)) {
            val stackString: String? = stackCache.remove(cid)
            if (!TextUtils.isEmpty(stackString)) {
                needUpdate = true
                s1thread.stackTraces = stackString?.split("\n")
                if (s1thread.name.isNullOrEmpty() && s1thread.stackTraces?.isNotEmpty() == true) {
                    s1thread.stackTraces?.get(0)?.let {str->
                        val end = str.lastIndexOf(".so")
                        val start = str.lastIndexOf("/")
                        if (start > 0 && end > start && end <= str.length - 3) {
                            s1thread.name = str.substring(start + 1, end + 3) + " ${s1thread.id}"
                        }
                    }
                }
            }
        }
        val name: String? = nameMap.remove(tid)
        if (!TextUtils.isEmpty(name)) {
            needUpdate = true
            s1thread.name = name
        }

        if (needUpdate) {
            ThreadInspector.threadUpdate(s1thread)
        }
    }

    @JvmStatic
    fun threadSetName(tid: Int, name: String) {
        val s1Thread = ThreadInspector.getThread(tid)
        if (s1Thread == null) {
            nameMap[tid] = name
        } else {
            s1Thread.name = name;
            ThreadInspector.threadUpdate(s1Thread)
        }
    }

    private fun filterStacktrace(s: StackTraceElement): Boolean {
        return (TextUtils.equals(s.getClassName(), S1ThreadHooker::class.java.getName())
                || s.getMethodName().contains("getStackTrace")
                || s.getMethodName().contains("getThreadStackTrace")
                || TextUtils.isEmpty(s.toString()))
    }

    @JvmStatic
    external fun nativeHookThread(type: Int): Int

    @JvmStatic
    external fun nativeUnhookThread(): Int

    @JvmStatic
    fun hookThread(type: Int = HOOK_ALL) {
        nativeHookThread(type)
    }

    @JvmStatic
    fun unhookThread() {
        nativeUnhookThread()
    }
}