## How to build the native libraries for the Tobii Tracker:

* Things to be installed:
  1. Git
  2. Java 1.8 (x86)
  3. Install 7zip
  4. Install Python 2.7 (x86)
  5. Install CMake
  6. Download and extract boost 1.60.0 (http://sourceforge.net/projects/boost/files/boost/1.60.0/boost_1_60_0.zip)
  7. Install Mingw-get (https://sourceforge.net/projects/mingw/files/Installer/mingw-get-setup.exe/download)
      a. Select packages:
          - mingw-developer-toolkit
          - mingw32-base
          - mingw32-gcc-g++
          - msys-base
      b. Installation apply
  8. Install Bonjour files (can be installed with Tobii Studio)

* Ater installion is done:

  1. Add C:\MinGW\bin to Path
  2. Open cmd and navigate to boost source directory
  3. Run: bootstrap.bat mingw
  4. Run: b2.exe --toolset=gcc --layout=tagged --build-type=complete --with-system --with-filesystem --with-thread --with-regex install link=shared install
  5. Close CMD
  6. Use Git Bash to clone itrace-archive
  7. Copy contents of tobii-analytics (32-bit) to MinGW:
    - tobii-analytics\Cpp\lib* -> C:\MinGW\lib (Just the containing files)
    - tobii-analytics\include\tobii -> C:\MingGW\include (the entire tobii folder)
  8. Disable realtime av protection from MS Defender Win 10
  9. open CMD and enter itrace-archive directory
  10. run: cd jni\TobiiTracker
  11. run: mkdir build
  12. run: cd build
  13. run: cmake .. -G "MinGW Makefiles"
  14. run: mingw32-make

  This will generate a dll called libTobiiTracker.dll
  15. Rename the dll to TobiiTracker.dll
  16. Copy the dll to the project root (iTrace-Archive\)
  17. In Perspective, under iTrace, select Tobii Tracker, if it is not already selected.
  18. Run the project. 
