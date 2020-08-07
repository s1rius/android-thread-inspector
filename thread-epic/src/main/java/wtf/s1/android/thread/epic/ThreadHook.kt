package wtf.s1.android.thread.epic

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import de.robv.android.xposed.DexposedBridge
import de.robv.android.xposed.XC_MethodHook
import wtf.s1.android.thread.ThreadInspector

object ThreadHook {

    fun hook() {
        try {
            class ThreadMethodHook : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                    val thread = param.thisObject as Thread
                    ThreadInspector.threadRun(thread)
                }
            }
            DexposedBridge.hookAllConstructors(
                Thread::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    protected override fun afterHookedMethod(param: MethodHookParam) {
                        super.afterHookedMethod(param)
                        val thread = param.thisObject as Thread
                        val clazz: Class<*> = thread.javaClass
                        if (clazz != Thread::class.java) {
                            DexposedBridge.findAndHookMethod(clazz, "run", ThreadMethodHook())
                        }
                        ThreadInspector.threadNew(thread)
                    }
                })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}