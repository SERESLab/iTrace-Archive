package edu.ysu.itrace.filters.fixation;

import edu.ysu.itrace.filters.OldRawGaze;

public class Fixation {
	private OldRawGaze rawGaze;
	private long duration;
	
	public Fixation(OldRawGaze rawGaze, long duration) {
		this.rawGaze = rawGaze;
		this.duration = duration;
	}
	
	public OldRawGaze getRawGaze() {
		return rawGaze;
	}
	
	public long getDuration() {
		return duration;
	}
}
