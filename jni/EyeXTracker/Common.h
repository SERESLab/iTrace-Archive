/*
 * Copyright 2013 Tobii Technology AB. All rights reserved.
 */

#pragma once
#include <stdint.h>
#include "tobiigaze_error_codes.h"
#include "tobiigaze.h"

/**
* This file provides some cross-platform glue code.
**/

#ifndef WIN32
#include <unistd.h>
#include <pthread.h>
#include <string.h>
#include <errno.h>
typedef pthread_t xthread_handle;
typedef void* xthread_retval;
#define XEXIT(code) exit(code)
#define XSLEEP(ms) Sleep(ms)
#define THREADFUNC_RETURN(retval) return (void*)retval;

xthread_handle xthread_create(void*(*function)(void*), void *argument)
{
    xthread_handle hthread;
    pthread_create(&hthread, NULL, function, argument);
    return hthread;
}

void xthread_join(xthread_handle hthread)
{
    pthread_join(hthread, NULL);
}

struct xcondition_variable
{
    pthread_mutex_t mutex;
    pthread_cond_t cv;
    int ready;
};

void xinitialize_cv(struct xcondition_variable* cv)
{
    pthread_mutex_init(&cv->mutex, NULL);
    pthread_cond_init(&cv->cv, NULL);
}

int xwait_until_ready(struct xcondition_variable* cv)
{
    int retval = 1;
    struct timespec timeout;

    memset(&timeout, 0, sizeof(struct timespec));
    timeout.tv_sec = time(NULL) + 3;

    pthread_mutex_lock(&cv->mutex);

    while (!cv->ready)
    {
        if (ETIMEDOUT == pthread_cond_timedwait(&cv->cv, &cv->mutex, &timeout) &&
            !cv->ready)
        {
            retval = 0;
            break;
        }
    }

    pthread_mutex_unlock(&cv->mutex);

    return retval;
}

void xsignal_ready(struct xcondition_variable* cv)
{
    pthread_mutex_lock(&cv->mutex);
    cv->ready = 1;
    pthread_mutex_unlock(&cv->mutex);

    pthread_cond_broadcast(&cv->cv);
}

void report_and_exit_on_error(tobiigaze_error_code error_code, const char *error_message)
{
    if (error_code)
    {
        fprintf(stderr, "Error: %d(%s).\n", error_code, error_message);
        fprintf(stderr, "Error code definitions are located in 'tobiigaze_error_codes.h'\n");
        fprintf(stderr, "Error means: %s\n", tobiigaze_get_error_message(error_code));
        fprintf(stderr, "Exiting...\n");
        XEXIT(1);
    }
}

#endif // !WIN32

/*#ifdef WIN32
#include <Windows.h>
typedef void* xthread_handle;
typedef DWORD xthread_retval;
#define XEXIT(code) exit(code)
#define XSLEEP(ms) Sleep(ms)
#define THREADFUNC_RETURN(retval) return retval;

xthread_handle xthread_create(void* function, void *argument)
{
    return CreateThread(
        NULL,                                       // default security attributes
        0,                                          // use default stack size
        (LPTHREAD_START_ROUTINE)function,           // thread function name
        argument,                                   // argument to thread function
        0,                                          // use default creation flags
        NULL);
}

void xthread_join(xthread_handle hthread)
{
    WaitForSingleObject(hthread, INFINITE);
}

struct xcondition_variable
{
    CRITICAL_SECTION cs;
    CONDITION_VARIABLE cv;
    int ready;
};

void xinitialize_cv(struct xcondition_variable* cv)
{
    InitializeCriticalSection(&cv->cs);
    InitializeConditionVariable(&cv->cv);
}

int xwait_until_ready(struct xcondition_variable* cv)
{
    int retval = 1;

    EnterCriticalSection(&cv->cs);

    while (!cv->ready)
    {
        if (!SleepConditionVariableCS(&cv->cv, &cv->cs, 3000))
        {
            retval = 0;
            break;
        }
    }

    LeaveCriticalSection(&cv->cs);

    return retval;
}

void xsignal_ready(struct xcondition_variable* cv)
{
    EnterCriticalSection(&cv->cs);
    cv->ready = 1;
    LeaveCriticalSection(&cv->cs);

    WakeAllConditionVariable(&cv->cv);
}

#endif // WIN32*/


