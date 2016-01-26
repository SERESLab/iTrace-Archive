Building the EyeX Tracker JNI Extension (under construction)
================================================================================
Here are the instructions for building the EyeX Tracker extension.

### Important Notes
 - You will be building a C++/CLI Java to .NET Connectivity bridge.
 - Visual Studio 2015 w/ Visual C++ will be used for the build process.
 - There are 3 main projects for connecting Java to .NET successfully:
 	1) Building the C# library that uses the .NET Tobii Gaze SDK (EyeXTrackerGaze.dll)
 	2) Modify the iTrace Java code to load the C++/CLI DLL from project 3
 		- We load .NET assemblies that our code depends upon (EyeXTrackerGaze.dll in this case)
 			dynamically from C++/CLI code by calling the “reigsterAssemblyHandler” native method
 	3) Build a C++/CLI library which uses the C# library created in project 1 (EyeXTrackerGaze.dll)
 		and implements JNI headers generated from Java native methods.

### Things to Install
 - Visual Studio 2015 w/ Visual C++ (Visual C++ is not installed by default)
 - JDK for your platform and architecture

### Download the Tobii EyeX Engine v1.8 and the Tobii EyeX Controller Drivers 
 - Download from: http://developer.tobii.com/downloads/
 - Use the 32bit version
 - Turn off the EyeX Engine in the EyeX Controller settings

### Download the Tobii Gaze SDK .NET API 2.1.0 Win32
 - Download from: http://developer.tobii.com/downloads/
 - Unzip the zip file to a directory of choice, e.g., “C:\tobiigazesdk”
 - Use the 32bit version

### Building EyeXTrackerGaze (C# Library that implements Tobii Gaze .Net SDK)
 - Create a C# project of type “Class library”
 - Open the Build/Configuration Manager dialog and change the active solution platform to x86 (the default is Any CPU). 
 	You will probably need to create a new solution platform to do this. We do this to match the bitness of
 	the TobiiGazeCore library (32-bit) we are using.
 - Add the Tobii.Gaze.Core.Net.dll as a dependency/reference
 - Don't include TobiiGazeCore32.dll, it will be loaded at runtime by Tobii.Gaze.Core.Net.dll
 - Include the C# file, EyeXTracker.cs, located in iTrace\jni\EyeXTracker\CSharp
 - Build the project to a DLL
 
### Modifying iTrace Java code to load the C++/CLI DLL from project 3
 - Full explanation here (still fine tuning the process)
 
### Building the C++/CLI library which uses the C# library created in project 1
 - Full explanation here (still fine tuning the process)
 - Note: This code acts as bridge between Java and C# (and managed code written in any language) code.
 - In Visual Studio 2015, create a CLR Class Library
 - Check the project properties to ensure this project’s output type is a .dll instead of .exe
 	and Common Language Runtime Support is enabled
 - Disable Precompiled Headers in "Configuration Properties\C/C++\Precompiled Headers"
 - Add C:\Program Files\Java\jdkX.X.X_XX\include to VC++ Include Directories in project setup
 - Add C:\Program Files\Java\jdkX.X.X_XX\include\win32 to VC++ Include Directories in project setup
 - Include the header files generated using “javah” from iTrace to this project
 - Add EyeXTrackerGaze.dll from project 1 as a dependency/reference
 - Compile the project to a DLL
 - Put the generated DLL in root of iTrace project