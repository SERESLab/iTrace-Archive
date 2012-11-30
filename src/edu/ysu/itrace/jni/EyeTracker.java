package edu.ysu.itrace.jni;

import java.awt.geom.Point2D;

/**
 * Models an eye-tracking device interface.
 */
public class EyeTracker {
	
	private static final String LIB_NAME = "eyetracker";

	static{
		System.loadLibrary(LIB_NAME);
	}
	
	/**
	 * Performs necessary initialization logic.
	 */
	public native void initialize();
	
	/**
	 * Performs necessary uninitialization logic.
	 */
	public native void uninitialize();
	
	/**
	 * Gets the current fixation point on the screen.
	 */
	public native Point2D getFixation();
}
