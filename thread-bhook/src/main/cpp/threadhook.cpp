//
// Created by al s1rius on 2021/11/30.
//

#include <string>
#include "thread_call_monitor.h"
#include <unordered_set>
#include <vector>

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
#include "third/str_util.h"

#define HACKER_JNI_VERSION    JNI_VERSION_1_6
#define HACKER_JNI_CLASS_NAME "wtf/s1/android/thread/bhook/S1ThreadHooker"
#define HACKER_JNI_METHOD_LOG_STACK_TRACE_ "threadCreate"
#define HACKER_JNI_METHOD_SIGN_LOG_STACK_TRACE "(I[B)V"
#define HACKER_JNI_METHOD_THREAD_START "threadStart"
#define HACKER_JNI_METHOD_SIGN_THREAD_START "(II)V"
#define HACKER_JNI_METHOD_THREAD_SET_NAME "threadSetName"
#define HACKER_JNI_METHOD_SIGN_THREAD_SET_NAME "(I[B)V"
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
static std::unordered_set<std::string> filter_set;

static bytehook_stub_t pthread_create_stub;
static bytehook_stub_t pthread_setname_np_stub;

class InnerMonitor : public ThreadCallMonitor {

    void handle(int what, void *data) override {

        auto *env = AttachEnv(jVM);
        if (nullptr == env) {
            LOG("looper not attach jvm");
            auto call = static_cast<ThreadCall *>(data);
            delete call;
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
                    jbyteArray jname = c_2_jbyteArray(env, name_copy);
                    if (jname != nullptr) {
                        env->CallStaticVoidMethod(hook_clazz, log_thread_set_name, call->tid,
                                                  jname);
                        env->ReleaseByteArrayElements(jname, reinterpret_cast<jbyte *>(name_copy),
                                                      0);
                    } else {
                        delete[] name_copy;
                    }
                }
                delete call;
                break;
            }

            case ACTION_CREATE: {
                auto call = static_cast<ThreadCallCreate *>(data);
                if (nullptr == call->stacktrace) return;
                char *stacktrace = strdup(call->stacktrace);
                auto jdli = c_2_jbyteArray(env, stacktrace);
                if (nullptr != jdli) {
                    env->CallStaticVoidMethod(hook_clazz,
                                              log_thread_create,
                                              call->cid, jdli);
                    env->ReleaseByteArrayElements(jdli, reinterpret_cast<jbyte *>(stacktrace), 0);
                } else {
                    delete[] stacktrace;
                }
                delete call;
                break;
            }
            default: {
                auto call = static_cast<ThreadCall *>(data);
                delete call;
            }
        }
    }
};

inline static JNIEnv *getEnv() {
    JNIEnv *env;
    if (JNI_OK != jVM->GetEnv((void **) &env, HACKER_JNI_VERSION)) {
        return nullptr;
    }
    return env;
}

inline static void thread_create_post(thread_holder *threadHolder, void *lr) {
    Dl_info info;
    memset(&info, 0, sizeof(info));
    dladdr(lr, &info);

    char *dli;
    asprintf(&dli, "\t at %s (%s) \r\n", info.dli_fname, info.dli_sname);

    if (nullptr == hook_clazz || nullptr == log_thread_create) return;
    JNIEnv *env = getEnv();

    // todo get java stacktrace in native
    if (nullptr == env) {
        monitor->t_create(new ThreadCallCreate(threadHolder->count, dli));
    } else {
        auto jdli = c_2_jbyteArray(env, dli);
        if (nullptr == jdli) {
            LOG("%d cant get native stack trace", threadHolder->count);
            return;
        }

        (env->CallStaticVoidMethod(hook_clazz,
                                   log_thread_create,
                                   (int) threadHolder->count,
                                   jdli));
        env->ReleaseByteArrayElements(jdli, reinterpret_cast<jbyte *>(dli), 0);
    }

}

static void *start_routine_delegate(thread_holder *arg) {

    auto tid = (int) syscall(SYS_gettid);
    char s[16] = "";
    prctl(PR_GET_NAME, (unsigned long) s, 0, 0, 0);

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
    delete arg;
    return result;
}

static int pthread_create_auto(pthread_t *pthread_ptr, pthread_attr_t const *attr,
                               void *(*start_routine)(void *), void *arg) {
    BYTEHOOK_STACK_SCOPE();
    auto *holder = new thread_holder(thread_count++);
    holder->start_routine = start_routine;
    holder->start_routine_arg = arg;

    int result = BYTEHOOK_CALL_PREV(pthread_create_auto, pthread_ptr, attr,
                                    reinterpret_cast<void *(*)(void *)>(start_routine_delegate),
                                    holder);
    thread_create_post(holder, BYTEHOOK_RETURN_ADDRESS());
    return result;
}

static void pthread_setname_np_auto(pthread_t pthread, const char *name) {
    BYTEHOOK_STACK_SCOPE();
    BYTEHOOK_CALL_PREV(pthread_setname_np_auto, pthread, name);

    if (nullptr == name) {
        return;
    }
    int tid = (int) syscall(SYS_gettid);
    char *name_copy = strdup(name);
    monitor->t_set_name(new ThreadCallSetName(tid, name_copy));
}

static bool allow_filter(const char *caller_path_name, void *arg) {
    (void) arg;

    std::string last_path = caller_path_name;
    std::size_t found = last_path.find_last_of("\\/");
    if (found > 0) last_path = last_path.substr(found + 1);

    if (filter_set.count(last_path)) {
        LOG("filter ignore %s", caller_path_name);
        return false;
    }
    return true;
}

static int hacker_thread_create_on(JNIEnv *env, jobject thiz, jint type) {
    (void) env, (void) thiz;
    delete monitor;
    monitor = new InnerMonitor();

    void *pthread_create_proxy = (void *) pthread_create_auto;
    void *pthread_setname_np_proxy = (void *) pthread_setname_np_auto;

    filter_set.insert("libc.so");
    filter_set.insert("libs1threadhook.so");
    if (1 == type) {
        std::vector<std::string> sys_lib_names = {
                "libbase.so",
                "libGLES_mali.so",
                "libunwindstack.so",
                "libutils.so",
                "libEGL.so",
                "libhwui.so",
                "libwebviewchromium.so",
                "libwebviewchromium_loader.so",
                "libwebviewchromium_plat_support.so",
        };
        filter_set.insert(sys_lib_names.begin(), sys_lib_names.end());
        pthread_create_stub = bytehook_hook_partial(allow_filter,
                                                    nullptr,
                                                    nullptr,
                                                    "pthread_create",
                                                    pthread_create_proxy,
                                                    nullptr,
                                                    nullptr);
        pthread_setname_np_stub = bytehook_hook_partial(allow_filter,
                                                        nullptr,
                                                        nullptr,
                                                        "pthread_setname_np",
                                                        pthread_setname_np_proxy,
                                                        nullptr,
                                                        nullptr);
    } else {
        pthread_create_stub = bytehook_hook_partial(allow_filter,
                                                    nullptr,
                                                    nullptr,
                                                    "pthread_create",
                                                    pthread_create_proxy,
                                                    nullptr,
                                                    nullptr);
        pthread_setname_np_stub = bytehook_hook_partial(allow_filter,
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
    monitor->quit();
    return 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;

    if (nullptr == vm) return JNI_ERR;
    jVM = vm;
    JNIEnv *env;
    if (JNI_OK != vm->GetEnv((void **) &env, HACKER_JNI_VERSION)) return JNI_ERR;
    if (nullptr == env) return JNI_ERR;

    jclass cls;
    if (nullptr == (cls = env->FindClass(HACKER_JNI_CLASS_NAME))) return JNI_ERR;
    hook_clazz = (jclass) env->NewGlobalRef(cls);

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
    return HACKER_JNI_VERSION;
}
