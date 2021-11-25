//
// Created by al s1rius on 2021/11/30.
//

#include <string>
#include "thread_call_monitor.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <cstdlib>
#include <unistd.h>
#include <cstdint>
#include <cinttypes>
#include <cstring>
#include <dlfcn.h>
#include <jni.h>
#include <ctime>
#include <android/log.h>
#include <syscall.h>
#include <sys/prctl.h>
#include "bytehook.h"
#include "threads.h"
#include "threadhook.h"

#define HACKER_JNI_VERSION    JNI_VERSION_1_6
#define HACKER_JNI_CLASS_NAME "wtf/s1/android/thread/bhook/S1ThreadHooker"
#define HACKER_JNI_METHOD_LOG_STACK_TRACE_ "threadCreate"
#define HACKER_JNI_METHOD_SIGN_LOG_STACK_TRACE "(ILjava/lang/String;)Ljava/lang/String;"
#define HACKER_JNI_METHOD_THREAD_START "threadStart"
#define HACKER_JNI_METHOD_SIGN_THREAD_START "(II)V"
#define HACKER_JNI_METHOD_THREAD_SET_NAME "threadSetName"
#define HACKER_JNI_METHOD_SIGN_THREAD_SET_NAME "(ILjava/lang/String;)V"
#define HACKER_TAG            "thread_hook"

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wgnu-zero-variadic-macro-arguments"
#define LOG(fmt, ...)  __android_log_print(ANDROID_LOG_INFO, HACKER_TAG, fmt, ##__VA_ARGS__)
#pragma clang diagnostic pop

#ifdef __cplusplus
}
#endif

static JavaVM *jVM;
static jclass hook_clazz;
static jmethodID log_thread_create;
static jmethodID log_thread_start;
static jmethodID log_thread_set_name;
static std::atomic_int32_t thread_count = 0;
static ThreadCallMonitor *monitor;

static bytehook_stub_t pthread_create_stub;
static bytehook_stub_t pthread_setname_np_stub;

inline static JNIEnv *getEnv() {
    JNIEnv *env;
    if (JNI_OK != jVM->GetEnv((void **) &env, HACKER_JNI_VERSION)) {
        return nullptr;
    }
    return env;
}

inline static char *thread_create_post(thread_holder *threadHolder, void *lr) {
    Dl_info info;
    memset(&info, 0, sizeof(info));
    dladdr(lr, &info);

    char *result;
    char *dli;
    asprintf(&dli, "\t at %s (%s) \r\n", info.dli_fname, info.dli_sname);

    if (nullptr == hook_clazz || nullptr == log_thread_create) return result;
    JNIEnv *env = getEnv();

    if (nullptr == env) {
        monitor->t_create(new ThreadCallCreate(threadHolder->count, dli));
    } else {

        char stack_trace[10000];
        auto jdli = (jstring) env->NewStringUTF(dli);
        auto java_stack_trace = (jstring) (env->CallStaticObjectMethod(hook_clazz,
                                                                       log_thread_create,
                                                                       (int) threadHolder->count,
                                                                       jdli));

        if (java_stack_trace) {
            const char *java_stack_to_c = env->GetStringUTFChars(java_stack_trace, (jboolean *) 0);

            result = strcat(stack_trace, java_stack_to_c);
            env->ReleaseStringUTFChars(java_stack_trace, java_stack_to_c);
        }

    }

    delete dli;
    return result;
}

static void *start_routine_delegate(thread_holder *arg) {

    auto tid = (int) syscall(SYS_gettid);
    char s[16] = "";
    prctl(PR_GET_NAME, (unsigned long) s, 0, 0, 0);
    LOG("routine p name %s tid %d, c %d", s, tid, (int) arg->count);

    JNIEnv *env = getEnv();
    if (nullptr != env) {
        env->CallStaticVoidMethod(hook_clazz, log_thread_start, tid, (int) arg->count);
    } else {
        auto c = arg->count;
        monitor->t_start(new ThreadCallStart(tid, c));
    }

    void *result = nullptr;
    if (nullptr != *(arg->start_routine)) {
        result = arg->start_routine(arg->start_routine_arg);
    }
    arg->start_routine = nullptr;
    arg->start_routine_arg = nullptr;
    free(arg);
    return result;
}

static int pthread_create_auto(pthread_t *pthread_ptr, pthread_attr_t const *attr,
                               void *(*start_routine)(void *), void *arg) {
    BYTEHOOK_STACK_SCOPE();
    auto *holder = new thread_holder(thread_count++);
    holder->start_routine = start_routine;
    holder->start_routine_arg = arg;

    monitor->t_create(new ThreadCallCreate(holder->count, nullptr));
    int result = BYTEHOOK_CALL_PREV(pthread_create_auto, pthread_ptr, attr,
                                    reinterpret_cast<void *(*)(void *)>(start_routine_delegate),
                                    holder);
    holder->stack_trace = thread_create_post(holder, BYTEHOOK_RETURN_ADDRESS());
    return result;
}

static void pthread_setname_np_auto(pthread_t pthread, const char *name) {
    BYTEHOOK_STACK_SCOPE();
    BYTEHOOK_CALL_PREV(pthread_setname_np_auto, pthread, name);

    if (nullptr == name) {
        return;
    }
    int tid = (int) syscall(SYS_gettid);


    JNIEnv *env = getEnv();
    if (nullptr != env) {
        char *name_copy = strdup(name);
        jstring jname = env->NewStringUTF(name_copy);
        env->CallStaticVoidMethod(hook_clazz, log_thread_set_name, tid, jname);
        env->ReleaseStringUTFChars(jname, name_copy);
    } else {
        char *name_copy = strdup(name);
        monitor->t_set_name(new ThreadCallSetName(tid, name_copy));
        delete name_copy;
    }
    LOG("jni setName name %s tid %d", name, tid);
}

static bool allow_filter(const char *caller_path_name, void *arg) {
    (void) arg;

    if (nullptr != strstr(caller_path_name, "libc.so")) return false;
    if (nullptr != strstr(caller_path_name, "libbase.so")) return false;
    //if (nullptr != strstr(caller_path_name, "liblog.so")) return false;
    if (nullptr != strstr(caller_path_name, "libunwindstack.so")) return false;
    if (nullptr != strstr(caller_path_name, "libutils.so")) return false;
    // ......

    return true;
}

static bool name_allow_filter(const char *caller_path_name, void *arg) {
    (void) arg;

    if (nullptr != strstr(caller_path_name, "libc.so")) return false;
    if (nullptr != strstr(caller_path_name, "libart.so")) return true;

    return true;
}

static bool allow_filter_for_hook_all(const char *caller_path_name, void *arg) {
    (void) arg;

    if (nullptr != strstr(caller_path_name, "libc.so")) return false;

    return true;
}

static int hacker_thread_create_on(JNIEnv *env, jobject thiz, jint type) {
    (void) env, (void) thiz;

    void *pthread_create_proxy = (void *) pthread_create_auto;
    void *pthread_setname_np_proxy = (void *) pthread_setname_np_auto;

    if (1 == type) {
        pthread_create_stub = bytehook_hook_partial(allow_filter,
                                                    nullptr,
                                                    nullptr,
                                                    "pthread_create",
                                                    pthread_create_proxy,
                                                    nullptr,
                                                    nullptr);
        pthread_setname_np_stub = bytehook_hook_partial(name_allow_filter,
                                                        nullptr,
                                                        nullptr,
                                                        "pthread_setname_np",
                                                        pthread_setname_np_proxy,
                                                        nullptr,
                                                        nullptr);
    } else {
        pthread_create_stub = bytehook_hook_partial(allow_filter_for_hook_all,
                                                    nullptr,
                                                    nullptr,
                                                    "pthread_create",
                                                    pthread_create_proxy,
                                                    nullptr,
                                                    nullptr);
        pthread_setname_np_stub = bytehook_hook_partial(allow_filter_for_hook_all,
                                                        nullptr,
                                                        nullptr,
                                                        "pthread_setname_np",
                                                        pthread_setname_np_proxy,
                                                        nullptr,
                                                        nullptr);
    }
    return 0;
}

static int hacker_thread_create_off(JNIEnv *env, jobject thiz) {
    (void) env, (void) thiz;

    if (nullptr != pthread_create_stub) {
        bytehook_unhook(pthread_create_stub);
        pthread_create_stub = nullptr;
    }

    if (nullptr != pthread_setname_np_stub) {
        bytehook_unhook(pthread_setname_np_stub);
        pthread_setname_np_stub = nullptr;
    }
    return 0;
}

class InnerMonitor : public ThreadCallMonitor {

    void handle(int what, void *data) override {
        auto tid = (int) syscall(SYS_gettid);
        char s[16] = "";
        prctl(PR_GET_NAME, (unsigned long) s, 0, 0, 0);
        LOG("monitor hand n= %s tid= %d ", s, tid);

        auto *env = AttachEnv(jVM);
        if (nullptr == env) {
            LOG("looper not attach jvm");
            return;
        }

        switch (what) {
            case ACTION_START: {
                auto call = static_cast<ThreadCallStart *>(data);
                env->CallStaticVoidMethod(hook_clazz, log_thread_start, call->tid, call->cid);
                delete call;
                break;
            }

            case ACTION_SET_NAME: {
                auto call = static_cast<ThreadCallSetName *> (data);
                if (nullptr != call->name) {
                    char *name_copy = strdup(call->name);
                    jstring jname = env->NewStringUTF(name_copy);
                    env->CallStaticVoidMethod(hook_clazz, log_thread_set_name, call->tid,
                                              jname);
                    env->ReleaseStringUTFChars(jname, name_copy);
                }
                delete call;
                break;
            }

            case ACTION_CREATE: {
                auto call = static_cast<ThreadCallCreate *>(data);
                auto jdli = (jstring) env->NewStringUTF(call->stacktrace);
                env->CallStaticObjectMethod(hook_clazz,
                                            log_thread_create,
                                            call->cid, jdli);
                delete jdli;
                delete call;
                break;
            }
            default: {

            }
        }
    }
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;

    if (nullptr == vm) return JNI_ERR;
    jVM = vm;
    JNIEnv *env;
    if (JNI_OK != vm->GetEnv((void **) &env, HACKER_JNI_VERSION)) return JNI_ERR;
    if (nullptr == env) return JNI_ERR;

    jclass cls;
    if (nullptr == (cls = env->FindClass(HACKER_JNI_CLASS_NAME))) return JNI_ERR;
    hook_clazz = (jclass)env->NewGlobalRef(cls);

    jmethodID mid;
    if (nullptr == (mid = env->GetStaticMethodID(
            hook_clazz,
            HACKER_JNI_METHOD_LOG_STACK_TRACE_,
            HACKER_JNI_METHOD_SIGN_LOG_STACK_TRACE)))
        return JNI_ERR;
    log_thread_create = mid;

    if (nullptr == (mid = env->GetStaticMethodID(hook_clazz, HACKER_JNI_METHOD_THREAD_START,
                                                 HACKER_JNI_METHOD_SIGN_THREAD_START)))
        return JNI_ERR;
    log_thread_start = mid;

    if (nullptr == (mid = env->GetStaticMethodID(hook_clazz, HACKER_JNI_METHOD_THREAD_SET_NAME,
                                                 HACKER_JNI_METHOD_SIGN_THREAD_SET_NAME)))
        return JNI_ERR;
    log_thread_set_name = mid;

    JNINativeMethod m[] = {
            {"nativeHookThread",   "(I)I", (void *) hacker_thread_create_on},
            {"nativeUnhookThread", "()I",  (void *) hacker_thread_create_off}
    };
    if (JNI_OK != env->RegisterNatives(cls, m, sizeof(m) / sizeof(m[0]))) return JNI_ERR;

    monitor = new InnerMonitor();
    return HACKER_JNI_VERSION;
}
