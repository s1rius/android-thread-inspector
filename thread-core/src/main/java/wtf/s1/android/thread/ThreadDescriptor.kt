package wtf.s1.android.thread

data class S1Thread constructor(
    var id: Long,
    var cid: Long,
    var name: String? = null,
    var group: String? = null,
    var state: String? = null,
    val priority: Int? = null,
    val isDaemon: Boolean? = null,
    var isInterrupted: Boolean? = null,
    var isAlive: Boolean? = null,
    var createTime: Long = System.currentTimeMillis(),
    var stackTraces: List<String>? = null
) {

    constructor(cid: Long, tid: Long, name: String? = null) : this(
        tid,
        cid,
        name,
        null,
        null,
        null,
        null,
        null,
        null,
        System.currentTimeMillis(),
        null
    )

    constructor(thread: Thread) : this(
        thread.id,
        thread.id,
        thread.name,
        thread.threadGroup.name,
        thread.state.ordinal.threadState(),
        thread.priority,
        thread.isDaemon,
        thread.isAlive,
        thread.isInterrupted
    )


    fun update(thread: Thread) {
        this.state = thread.state.ordinal.threadState()
        this.isInterrupted = thread.isInterrupted
        this.isAlive = thread.isAlive
    }

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
        Thread.State.TIMED_WAITING.ordinal -> "timed-waiting"
        else -> "unknown"
    }
}