//
// Created by s1rius on 2021/12/14.
//

#ifndef ANDROID_THREAD_INSPECTOR_STR_UTIL_H
#define ANDROID_THREAD_INSPECTOR_STR_UTIL_H

#include <jni.h>

jbyteArray c_2_jbyteArray(JNIEnv *env, char* cstr) {
    if (nullptr == cstr || nullptr == env) {
        return nullptr;
    }

    jbyteArray ba = env->NewByteArray(strlen(cstr));
    env->SetByteArrayRegion(ba, 0, strlen(cstr), reinterpret_cast<const jbyte *>(cstr));
    return ba;
}

#endif //ANDROID_THREAD_INSPECTOR_STR_UTIL_H
