package edu.ysu.itrace.filters;

public class Fixation {
	private RawGaze rawGaze;
	private long duration;
	
	public Fixation(RawGaze rawGaze, int duration) {
		this.rawGaze = rawGaze;
		this.duration = duration;
	}
	
	public RawGaze getRawGaze() {
		return rawGaze;
	}
	
	public long getDuration() {
		return duration;
	}
}
