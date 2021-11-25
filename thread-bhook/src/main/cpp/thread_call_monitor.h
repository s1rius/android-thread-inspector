//
// Created by al s1rius on 2021/11/30.
//

#ifndef ANDROID_THREAD_INSPECTOR_THREAD_CALL_MONITOR_H
#define ANDROID_THREAD_INSPECTOR_THREAD_CALL_MONITOR_H

#include "thread_call.h"
#include "third/looper.h"

class ThreadCallMonitor: public looper {

public:
    ThreadCallMonitor();
    ~ThreadCallMonitor();

    void t_create(ThreadCallCreate* call);
    void t_start(ThreadCallStart* call);
    void t_set_name(ThreadCallSetName* call);

    void handle(int what, void *data);
    void post(int what, void *data);

private:
};

#endif //ANDROID_THREAD_INSPECTOR_THREAD_CALL_MONITOR_H
