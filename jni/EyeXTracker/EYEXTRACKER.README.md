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

### Download the Tobii EyeX Tracker SDK v1.5
 - Download from: http://developer.tobii.com/downloads/
 - Extract all libs in CPP to your MinGW lib/ directory and all includes in CPP
   to your MinGW include/ directory
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