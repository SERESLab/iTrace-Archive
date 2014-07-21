package edu.ysu.itrace.trackers;

import java.io.IOException;

import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.IEyeTracker;
import edu.ysu.itrace.exceptions.CalibrationException;

/**
 * Provides an interface for the EyeX eye tracker.
 *
 */
public class EyeXTracker implements IEyeTracker {

	private double xDrift = 0;
    private double yDrift = 0;
	
    /**
     * Load the libraries that are necessary to communicate with the EyeX eye tracker.
     * It is important to first load the Tobii Client library and then the
     * EyeXTracker library.
     */
    static {
    	System.loadLibrary("jni/Tobii.EyeX.Client");	
        System.loadLibrary("jni/edu_ysu_itrace_trackers_EyeXTracker");
    }

    private native void disconnectEyeTracker();
    private native boolean connectEyeTracker();
   
    public static void main(String... args) {
    	boolean s = new EyeXTracker().connectEyeTracker();
		System.out.println(s);
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void calibrate() throws CalibrationException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startTracking() throws IOException {
		boolean success = connectEyeTracker();
		if (success) {
			System.out.println("Sucessfully connected");
		} else {
			System.out.println("Not yet connected");
		}
	}
	@Override
	public void stopTracking() throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Gaze getGaze() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void displayCrosshair(boolean enabled) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setXDrift(int drift) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setYDrift(int drift) {
		// TODO Auto-generated method stub
		
	}
    
}