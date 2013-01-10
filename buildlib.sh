#!/bin/bash

DIR_THIS=$(pwd)

cd ./jni

g++ -shared -o libeyetracker.so -I/usr/java/include -I/usr/java/include/linux eyetracker.cpp

mv libeyetracker.so ..
cd $DIR_THIS
