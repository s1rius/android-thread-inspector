//
// Created by al s1rius on 2021/11/30.
//
#include "thread_call_monitor.h"

void ThreadCallMonitor::t_create(ThreadCallCreate* call) {
    post(ACTION_CREATE, call);
}

void ThreadCallMonitor::t_start(ThreadCallStart* call) {
    post(ACTION_START, call);
}

void ThreadCallMonitor::t_set_name(ThreadCallSetName* call) {
    post(ACTION_SET_NAME, call);
}

void ThreadCallMonitor::handle(int what, void *data) {
}

void ThreadCallMonitor::post(int what, void *data) {
    looper::post(what, data);
}

ThreadCallMonitor::ThreadCallMonitor(): looper() {}

ThreadCallMonitor::~ThreadCallMonitor() = default;

