package edu.ysu.itrace;

public interface IEyeTracker {
	public void close();
	public void startTracking();
	public void stopTracking();
	public Gaze getGaze();
}
