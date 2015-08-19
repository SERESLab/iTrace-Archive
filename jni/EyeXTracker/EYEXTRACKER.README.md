Building the EyeX Tracker JNI Extension
================================================================================
Here are the instructions for building the EyeX Tracker extension.

### Important Notes
 - You are required to use a UNIX system or MinGW32/MSYS for Windows.
 - The GNU Compiler Collection for C++ is required.

### Things to Install
 - CMake
 - JDK for your platform and architecture

### Download the Tobii EyeX Engine v1.4 and the Tobii EyeX Controller Drivers 
 - Download from: http://developer.tobii.com/downloads/
 - Use the 32bit version
 - Turn off the EyeX Engine in the EyeX Controller settings

### Download the Tobii Gaze SDK v4.1
 - Download from: http://developer.tobii.com/downloads/
 - Extract all libs in CPP to your MinGW lib/ directory and all includes in CPP
   to your MinGW include/ directory
 - Extract the Common.h file from the Samples/Common folder (in the Tobii Gaze SDK download) to your MinGW include/ directory
 - Modify the Common.h file for your specific platform
 - Use the 32bit version

### Building EyeXTracker
 - Create a directory build/ within this directory.
 - Open a UNIX shell or Windows command line and enter the build/ directory.
 - Run: cmake ..
   - You may need to use CMake's -G flag to specify what type of build files to
     create
 - Example:
   - cmake .. -G "MinGW Makefiles"
   - mingw32-make
 - Copy the .so/.dll file created by make to the project root as
   libEyeXTracker.so on UNIX systems or libEyeXTracker.dll on NT systems.