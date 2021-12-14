//
// Created by s1rius on 2021/12/2.
//

#include "thread_call.h"

ThreadCallStart::~ThreadCallStart() = default;

ThreadCallSetName::~ThreadCallSetName() {
    delete[] name;
}

ThreadCallCreate::~ThreadCallCreate() {
    delete[] stacktrace;
}
