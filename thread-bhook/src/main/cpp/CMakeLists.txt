cmake_minimum_required(VERSION 3.18.1)
project(thread-bhook)

if(DEPENDENCY_ON_LOCAL_LIBRARY)
    # import bytehook from local-project
    string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE_DIRNAME)
    if(${BUILD_TYPE_DIRNAME} STREQUAL "relwithdebinfo")
        set(BUILD_TYPE_DIRNAME "release")
    endif()
    set(BYTEHOOK_BASE ${CMAKE_CURRENT_SOURCE_DIR}/../../../../bytehook)
    add_library(bytehook::bytehook SHARED IMPORTED)
    set_target_properties(bytehook::bytehook PROPERTIES
            IMPORTED_LOCATION ${BYTEHOOK_BASE}/build/intermediates/stripped_native_libs/${BUILD_TYPE_DIRNAME}/out/lib/${ANDROID_ABI}/libbytehook.so
            INTERFACE_INCLUDE_DIRECTORIES ${BYTEHOOK_BASE}/src/main/cpp/include)
else()
    # import bytehook from maven
    find_package(bytehook REQUIRED CONFIG)
endif()

# libs1threadhook.so
add_library(s1threadhook
        SHARED
        thread_call_monitor.cpp
        threadhook.cpp
        thread_call.cpp
        third/looper.cpp
        )
target_compile_features(s1threadhook PUBLIC cxx_std_17)
#target_compile_options(s1threadhook PUBLIC -Weverything -Werror)
target_link_libraries(s1threadhook bytehook::bytehook log)
