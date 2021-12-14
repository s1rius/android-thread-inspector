//
// Created by al s1rius on 2021/11/30.
//
#ifndef S1_THREAD_HOOK_H
#define S1_THREAD_HOOK_H

struct thread_holder{
    void *(*start_routine)(void *);
    void *start_routine_arg;
    size_t count;
    thread_holder(){}
    thread_holder(size_t c): count(c) {}
};

JNIEnv *AttachEnv(JavaVM* java_vm_) {
    JNIEnv *env = nullptr;
    int status = java_vm_->GetEnv((void **)&env, JNI_VERSION_1_6);
    if (status == JNI_EDETACHED || env == nullptr) {
        status = java_vm_->AttachCurrentThread(&env, nullptr);
        if (status < 0) {
            env = nullptr;
        }
    }
    return env;
}

#endif
