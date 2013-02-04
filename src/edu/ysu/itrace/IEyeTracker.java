package edu.ysu.itrace;

public interface IEyeTracker {
	public boolean close();
	public boolean startTracking();
	public boolean stopTracking();
	public Gaze getGaze();
}
