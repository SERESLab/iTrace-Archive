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
 - Either install the Boost 1.40 package through your operating system's
   package manager or run the build_boost.sh script.
   - Required boost packages: system, filesystem, thread, regex.
   - build_boost.sh can be used in MSYS in Windows environements.

### Download the Tobii SDK v3.0
 - Download from: http://www.tobii.com/en/eye-tracking-research/global/landingpages/analysis-sdk-30/
 - Extract all libs in CPP to your MinGW lib/ directory and all includes in CPP
   to your MinGW include/ directory

### Building TobiiTracker
 - Create a directory build/ within this directory.
 - Open a UNIX shell or Windows command line and enter the build/ directory.
 - Run: cmake .. -DBoost_DIR=/usr/local/ -DBoost_INCLUDE_DIR=/usr/local/include/
   - Naturally, if boost is installed elsewhere, change the above Boost paths
   - You may need to use CMake's -G flag to specify what type of build files to
     create
 - Copy the .so/.dll file created by make to the project root as
   libTobiiTracker.so on UNIX systems or TobiiTracker.dll on NT systems.