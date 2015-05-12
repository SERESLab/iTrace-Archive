# iTrace - Plugin for an Eye-Enabled IDE

iTrace is a plugin for the Eclipse IDE which enables it to get information from
eye trackers and the IDE to obtain meaningful information from these two data
sources. iTrace hopes to be a general tool, but it is also specialised for
traceability.


## Requirements
* Project must be checked out as "iTrace" in your workspace.
* Eclipse IDE (Windows users should install Eclipse IDE for Java EE Developers)
* Java development kit (JDK)
* Apache IvyDE Eclipse Plugin (https://ant.apache.org/ivy/ivyde/)
* C++ compiler: g++ on UNIX or Microsoft Visual Studio on Microsoft Windows
* Java native interface (JNI) headers


## How to Build Native Tracker Drivers
1. Use CMake to export build files for the libraries in the jni/ directory.
2. After building the the libraries, verify that they have the characters
   "lib" at the beginning of the file name for UNIX-based platforms, and do not
   for Windows-based platforms.
3. Move all dynamic libraries from the jni/ directory to the project root.


## How to Build and Run
1. Install all requirements and resolve Ivy dependencies (secondary click
   project, then click Ivy -> Resolve, then refresh the project).
2. Build and install plugin binaries or click "Run" from the Eclipse workspace
   and choose "Eclipse Application".
3. Open the "iTrace" perspective. If it is not visible, click the
   "Open Perspective" icon next to the "Java" perspective icon (by default in
   the top right corner) and choose "iTrace" from the list.
4. Open the iTrace controller view on the bottom panel.


## Developer Guidelines
* Master is reserved for stable code.
* Develop all new features as a new branch.
  * Keep this branch up to date with master.
* When a branch is completed, do not merge it into master. Create a pull request
  and possibly assign a reviewer. The code reviewer will merge your code into
  master.
* Minimise inclusion of automatically generated files (i.e. if a file in the
  project can be automatically generated from another file in the project, there
  is no reason to include the generated file).
  * Executable code is an example of this. Do not include build directories.
* If possible, use Ivy to manage dependencies instead of including third-party
  libraries in this repository.
* Use CMake to build all JNI libraries.
* Never check in files of which you do not have the legal rights to publish.


## Style Guide for Developers
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
