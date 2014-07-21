#include <jni.h>
#include <stdio.h>
#include "edu_ysu_itrace_trackers_EyeXTracker.h"

JNIEXPORT void JNICALL
Java_edu_ysu_itrace_trackers_EyeXTracker_connectEyeTracker(JNIEnv *env, jobject obj)
{
	printf("Hello World!\n");
	return;
}