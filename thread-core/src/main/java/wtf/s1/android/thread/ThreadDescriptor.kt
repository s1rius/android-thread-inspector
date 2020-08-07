package wtf.s1.android.thread

import androidx.annotation.Keep

@Keep
data class S1ThreadGroup(val name: String) {
    constructor(threadGroup: ThreadGroup): this(threadGroup.name)
}

@Keep
data class S1Thread(val id: Long,
                    val name: String,
                    val group: S1ThreadGroup,
                    var state: String,
                    val priority: Int,
                    val isDaemon: Boolean,
                    val isInterrupted: Boolean,
                    val createTime: Long = System.currentTimeMillis()
                     ) {

    constructor(thread: Thread): this(
        thread.id,
        thread.name,
        S1ThreadGroup(thread.threadGroup),
        thread.state.ordinal.threadState(),
        thread.priority,
        thread.isDaemon,
        thread.isInterrupted)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as S1Thread

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "S1Thread(id=$id, name='$name', " +
                "group=$group, " +
                "state='$state', " +
                "priority=$priority, " +
                "isDaemon=$isDaemon, " +
                "isInterrupted=$isInterrupted, " +
                "createTime=$createTime)"
    }
}

fun Int.threadState(): String {
    return when (this) {
        Thread.State.NEW.ordinal -> "new"
        Thread.State.BLOCKED.ordinal -> "blocked"
        Thread.State.RUNNABLE.ordinal -> "runnable"
        Thread.State.TERMINATED.ordinal -> "terminated"
        Thread.State.WAITING.ordinal -> "waiting"
        Thread.State.TIMED_WAITING.ordinal-> "timed-waiting"
        else -> "unknown"
    }
}