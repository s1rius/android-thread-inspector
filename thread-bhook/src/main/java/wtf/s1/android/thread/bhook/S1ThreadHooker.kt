@file:Suppress("unused")

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
    private const val DEBUG = false
    private const val TAG = "thread_hook"

    //ugly fixme
    private val c2stack = ConcurrentHashMap<Int, String>()
    private val c2t = ConcurrentHashMap<Int, Int>()
    private val t2c = ConcurrentHashMap<Int, Int>()
    private val t2name = ConcurrentHashMap<Int, String>()

    init {
        System.loadLibrary("s1threadhook")
    }

    @SuppressLint("DefaultLocale")
    @JvmStatic
    fun threadCreate(cid: Int, nativeStack: ByteArray?) {
        val sb: StringBuilder = StringBuilder()
        if (nativeStack != null) {
            sb.append(String(nativeStack))
            val stackTrace: Array<StackTraceElement> = Thread.currentThread().stackTrace
            for (s: StackTraceElement in stackTrace) {
                if (!filterStacktrace(s)) {
                    sb.append("\t at ")
                        .append(s.toString())
                        .append('\n')
                }
            }

            c2stack[cid] = sb.toString()
            if (c2t.contains(cid)) {
                c2t[cid]?.let { tid->
                    updateThread(cid, tid)
                }
            }
            if (DEBUG) Log.i(TAG, "catch stacktrace cid = $cid stack = ${stackTrace.size}")
        }
    }

    @JvmStatic
    fun threadStart(tid: Int, cid: Int) {
        c2t[cid] = tid
        t2c[tid] = cid
        updateThread(cid, tid)
    }

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    private fun updateThread(cid: Int, tid: Int) {

        var s1thread = ThreadInspector.getThread(tid)

        if (s1thread == null) {
            s1thread = S1Thread(cid.toLong(), tid.toLong())
        }

        var needUpdate = false
        if (c2stack.containsKey(cid)) {
            val stackString: String? = c2stack.remove(cid)
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
        val name: String? = t2name.remove(tid)
        if (!TextUtils.isEmpty(name)) {
            needUpdate = true
            s1thread.name = name
        }

        if (needUpdate) {
            ThreadInspector.threadUpdate(s1thread)
        } else {
            ThreadInspector.threadCreate(s1thread)
        }
        if (DEBUG) Log.i(TAG, "$s1thread")
    }

    @JvmStatic
    fun threadSetName(tid: Int, name: ByteArray?) {
        if (name == null) return
        t2name[tid] = String(name)
        t2c[tid]?.let {
            updateThread(it, tid)
        }
        if (DEBUG) Log.i(TAG, "catch name tid = $tid name = ${String(name)}")
    }

    private fun filterStacktrace(s: StackTraceElement): Boolean {
        return (TextUtils.equals(s.className, S1ThreadHooker::class.java.name)
                || s.methodName.contains("getStackTrace")
                || s.methodName.contains("getThreadStackTrace")
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