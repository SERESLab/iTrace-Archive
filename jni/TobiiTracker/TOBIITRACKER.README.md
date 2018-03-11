## How to build the native libraries for the Tobii Tracker:

* Things to be installed:
  - Git
  - Java 1.8 (x86)
  - Install 7zip
  - Install Python 2.7 (x86)
  - Install CMake
  - Download and extract boost 1.60.0 (http://sourceforge.net/projects/boost/files/boost/1.60.0/boost_1_60_0.zip)
  - Install Mingw-get (https://sourceforge.net/projects/mingw/files/Installer/mingw-get-setup.exe/download)
      - Select packages:
          - mingw-developer-toolkit
          - mingw32-base
          - mingw32-gcc-g++
          - msys-base
      - Installation apply
  - Install Bonjour files (can be installed with Tobii Studio)

* Ater installion is done:

  - Add C:\MinGW\bin to Path
  - Open cmd and navigate to boost source directory
  - Run: bootstrap.bat mingw
  - Run: b2.exe --toolset=gcc --layout=tagged --build-type=complete --with-system --with-filesystem --with-thread --with-regex install link=shared install
  - Close CMD
  - Use Git Bash to clone itrace-archive
  - Copy contents of tobii-analytics (32-bit) to MinGW:
    - tobii-analytics\Cpp\lib* -> C:\MinGW\lib (Just the containing files)
    - tobii-analytics\include\tobii -> C:\MingGW\include (the entire tobii folder)
  - Disable realtime av protection from MS Defender Win 10
  - open CMD and enter itrace-archive directory
  - run: cd jni\TobiiTracker
  - run: mkdir build
  - run: cd build
  - run: cmake .. -G "MinGW Makefiles" (Make sure Cmake is in path)
  - run: mingw32-make
  - set boost/lib to path
  - set TobiiAnalytics/CPP/lib to path
  

  This will generate a dll called libTobiiTracker.dll
  - Rename the dll to TobiiTracker.dll
  - Copy the dll to the project root (iTrace-Archive\)
  - In Perspective, under iTrace, select Tobii Tracker, if it is not already selected.
  - Run the project.
