package wtf.s1.android.thread.flipper

import com.facebook.flipper.core.FlipperArray
import com.facebook.flipper.core.FlipperObject
import wtf.s1.android.thread.S1Thread

fun S1Thread.toFlipperObject(): FlipperObject {

    val builder = FlipperObject.Builder()
        .put("id", this.id)
        .put("name", this.name)
        .put("group", this.group)
        .put("createAt", this.createTime)
        .put("priority", this.priority)
        .put("state", this.state)
        .put("daemon", "${this.isDaemon}")
        .put("alive","${this.isAlive}")

    if (this@toFlipperObject.stackTraces == null) {
        return builder.build()
    }

    val array = FlipperArray.Builder()
    val traces = this@toFlipperObject.stackTraces!!

    for (index in 0 until traces.size) {
        traces.getOrNull(index)?.let {t->
            array.put(t)
        }
    }
    builder.put("stacktraces", array.build())

    return builder.build()
}