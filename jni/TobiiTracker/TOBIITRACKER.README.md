Building the Tobii Tracker JNI Extension
================================================================================
Here are the instructions for building the Tobii Tracker extension.

### Important Notes
 - You are required to use a UNIX system or MinGW32/MSYS for Windows.
 - The GNU Compiler Collection for C++ is required.

### Things to Install
 - CMake
 - JDK for your platform and architecture

### Build Boost
 - Download Boost from: http://www.boost.org/users/download/
 - Extract the files to the directory of your choosing.
 - Open a command prompt in that location.
 - Run these commands:
   - bootstrap gcc
   - b2 toolset=gcc
 - Afterward copy the subfolder boost into MinGW/include and the contents of stage/lib into MinGW/lib. 
   - Or add those locations to the include and lib paths of MinGW

### Download the Tobii SDK v3.0
 - Download from: http://www.tobii.com/en/eye-tracking-research/global/landingpages/analysis-sdk-30/
 - Extract all libs in CPP to your MinGW lib/ directory and all includes in CPP
   to your MinGW include/ directory
 - Use the 32bit version

### Building TobiiTracker
 - Create a directory build/ within this directory.
 - Open a UNIX shell or Windows command line and enter the build/ directory.
 - Run the following commands:
   - cmake .. -G "MinGW Makefiles"
   - mingw32-make
 - Copy the .so/.dll file created by make to the project root as
   libTobiiTracker.so on UNIX systems or TobiiTracker.dll on NT systems.
