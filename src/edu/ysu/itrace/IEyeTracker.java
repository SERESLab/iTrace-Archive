package edu.ysu.itrace;

import java.io.IOException;

public interface IEyeTracker {
	/**
	Closes eye tracker connection.
	*/
	public void close();
	/**
	Starts eye tracking and collecting data.
	@throws IOException Failed to start eye tracking.
	*/
	public void startTracking() throws IOException;
	/**
	Stops eye tracking. Current data still available via getGaze, but no new gaze
	data is added.
	@throws IOException Failed to stop eye tracking.
	*/
	public void stopTracking() throws IOException;
	/**
	Gets next stored gaze data, or null if no more exists.
	*/
	public Gaze getGaze();
}
