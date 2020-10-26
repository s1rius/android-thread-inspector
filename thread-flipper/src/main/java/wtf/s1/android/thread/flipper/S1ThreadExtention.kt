package wtf.s1.android.thread.flipper

import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import wtf.s1.android.thread.S1Thread

fun S1Thread.toFlipperObject(): FlipperObject {

    return FlipperObject.Builder()
        .put("id", this.id)
        .put("name", this.name)
        .put("group", this.group.name)
        .put("createAt", this.createTime)
        .put("priority", this.priority)
        .put("state", this.state)
        .put("daemon", "${this.isDaemon}")
        .put("alive","${this.isAlive}")
        .put("stacktraces", FlipperArray.Builder().apply {
            this@toFlipperObject.stackTraces?.forEach {
                this.put(it)
            }
        }.build())
        .build()

}