package edu.ysu.itrace.trackers;

/**
 * Provides an interface for the EyeX eye tracker.
 *
 */
public class EyeXTracker {

	private double xDrift = 0;
    private double yDrift = 0;
	
    static {
    	System.loadLibrary("jni/Tobii.EyeX.Client");	
        System.loadLibrary("jni/edu_ysu_itrace_trackers_EyeXTracker");
    }
    
    public static void main(String... args) {
    	new EyeXTracker().connectEyeTracker();
    }
    
    private native void connectEyeTracker();

}