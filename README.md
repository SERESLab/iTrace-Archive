DESCRIPTION
===========
This Eclipse plugin interfaces with an eye-tracking device to utilize fixation information.


REQUIREMENTS
============
* Project must be checked out as "iTrace" in your workspace.
* Eclipse IDE
* Java development kit (JDK)
  * Graphical Editing Framework GEF SDK
* Apache IvyDE Eclipse Plugin (https://ant.apache.org/ivy/ivyde/)
* C++ compiler: g++ on UNIX or Microsoft Visual Studio on Microsoft Windows
* Java native interface (JNI) headers


HOW TO BUILD: WINDOWS
=====================
1) Edit "buildlib.bat" to ensure that LOC_JNI_H is set to the directory containing "jni.h" and LOC_JNI_MD_H is set to the directory containing "jni_md.h".
2) Open the Visual Studio command prompt by going to Start -> Programs -> Visual Studio -> Visual Studio Tools.
3) Navigate to the root directory of the project and execute "buildlib.bat". This will build "eyetracker.dll" in the project's root directory.
4) It may be necessary to refresh the project in the Eclipse workspace and/or clean the project before running.


HOW TO RUN
==========
1) Install all requirements and resolve Ivy dependencies (secondary click project, then click Ivy -> Resolve, then refresh the project).
2) Build and install plugin binaries or click "Run" from the Eclipse workspace and choose "Eclipse Application".
3) Open the "itrace" perspective. If it is not visible, click the "Open Perspective" icon next to the "Java" perspective icon (by default in the top right corner) and choose "itrace" from the list.
4) Open the itrace controller view on the bottom panel.

STYLE GUIDE FOR DEVELOPERS
==========
Try to use [Java code conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html).
Below is an example from Eclipse.

    /**
     * A sample source file for the code formatter preview
     */

    package mypackage;

    import java.util.LinkedList;

    public class MyIntStack {
        private final LinkedList fStack;

        public MyIntStack() {
            fStack = new LinkedList();
        }

        public int pop() {
            return ((Integer) fStack.removeFirst()).intValue();
        }

        public void push(int elem) {
            fStack.addFirst(new Integer(elem));
        }

        public boolean isEmpty() {
            return fStack.isEmpty();
        }
    }
