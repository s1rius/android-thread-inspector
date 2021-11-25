package wtf.s1.android.sample;

import android.util.Log;

public class NativeHookee {

    static {
        System.loadLibrary("hookee");
    }

    public static void testThreadCreate() {
        new Thread(){
            @Override
            public void run() {
                super.run();
            }
        }.start();
    }

    public static void nCreate() {
        nativeThreadCreate();
    }

    private static native void nativeThreadCreate();
}
