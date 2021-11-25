//
// Created by al s1rius on 2021/12/1.
//

#ifndef ANDROID_THREAD_INSPECTOR_THREAD_CALL_H
#define ANDROID_THREAD_INSPECTOR_THREAD_CALL_H


enum ThreadAction {
    ACTION_CREATE,
    ACTION_START,
    ACTION_SET_NAME
};

class ThreadCall {

};

class ThreadCallStart : public ThreadCall {
public:
    ThreadCallStart(int tid, int cid) : tid(tid), cid(cid) {}

    int tid;
    int cid;
};

class ThreadCallSetName : public ThreadCall {

public:
    ThreadCallSetName(int tid, char *name) : tid(tid), name(name) {}

    int tid;
    char *name;
};

class ThreadCallCreate : public ThreadCall {

public:
    ThreadCallCreate(int cid, char *stacktrace) : cid(cid), stacktrace(stacktrace) {}
    int cid;
    char *stacktrace;
};


#endif //ANDROID_THREAD_INSPECTOR_THREAD_CALL_H
