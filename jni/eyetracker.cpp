#include "eyetracker.h"

JNIEXPORT void JNICALL
Java_edu_ysu_itrace_jni_EyeTracker_initialize(JNIEnv *env, jobject obj){
  
}

JNIEXPORT void JNICALL
Java_edu_ysu_itrace_jni_EyeTracker_uninitialize(JNIEnv *env, jobject obj){
  
}

JNIEXPORT jobject JNICALL
Java_edu_ysu_itrace_jni_EyeTracker_getFixation(JNIEnv *env, jobject obj){
  
  double x = 3.14;
  double y = 2.72;
  
  // construct a new Point2D.Double object to return the fixation coordinates
  jclass cls = env->FindClass("java/awt/geom/Point2D$Double");
  jmethodID constructor = env->GetMethodID(cls, "<init>", "(DD)V");
  return env->NewObject(cls, constructor, x, y);
}
