@echo off
set LOC_JNI_H="C:\Program Files\Java\jdk1.6.0_24\include"
set LOC_JNI_MD_H="C:\Program Files\Java\jdk1.6.0_24\include\win32"
set IN_FILE=".\jni\eyetracker.cpp"
set OUT_DLL="eyetracker.dll"
cl -I%LOC_JNI_H% -I%LOC_JNI_MD_H% -LD %IN_FILE% -Fe%OUT_DLL%
