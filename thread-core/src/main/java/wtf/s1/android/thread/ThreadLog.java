package wtf.s1.android.thread;

import android.util.Log;

import androidx.annotation.Keep;

@Keep
public class ThreadLog {

    private static final String TAG = "ThreadLog";

    public static void init() {
        Log.i(TAG, "log thread");
    }

    public static void inject(Thread thread) {
        ThreadLogImp.INSTANCE.threadNew(thread);
    }
}
