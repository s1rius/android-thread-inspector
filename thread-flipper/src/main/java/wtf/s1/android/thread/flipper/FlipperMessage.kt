package wtf.s1.android.thread.flipper

import com.facebook.flipper.core.FlipperObject
import org.json.JSONArray
import org.json.JSONObject
import wtf.s1.android.thread.S1Thread

/**
 *   newThread: S1Thread;
 *   threads: Array<S1Thread>;
 */
class FlipperMessage(var newThread: S1Thread? = null,
                     var threads: MutableList<S1Thread>? = null) {

    fun toFlipperObject(): FlipperObject {
        val jsonArray = JSONArray()
        threads?.forEach {
            jsonArray.put(JSONObject(it.toFlipperObject().toJsonString()))
        }
        return FlipperObject.Builder()
            .put("newThread", newThread?.toFlipperObject())
            .put("threads", jsonArray)
            .build()

    }
}