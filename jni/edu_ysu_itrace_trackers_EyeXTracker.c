#include <Windows.h>
#include <stdio.h>
#include <conio.h>
#include <assert.h>
#include "edu_ysu_itrace_trackers_EyeXTracker.h"
#include "eyex/EyeX.h"

#pragma comment (lib, "Tobii.EyeX.Client.lib")

JNIEXPORT void JNICALL
Java_edu_ysu_itrace_trackers_EyeXTracker_connectEyeTracker(JNIEnv *env, jobject obj)
{
printf("Hello World!\n");
return;
}