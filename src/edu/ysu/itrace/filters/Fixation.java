package edu.ysu.itrace.filters;

public class Fixation {
	private RawGaze rawGaze;
	private int duration;
	
	public Fixation(RawGaze rawGaze, int duration) {
		this.rawGaze = rawGaze;
		this.duration = duration;
	}
	
	public RawGaze getRawGaze() {
		return rawGaze;
	}
	
	public int getDuration() {
		return duration;
	}
}
