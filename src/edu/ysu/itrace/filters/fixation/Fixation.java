package edu.ysu.itrace.filters.fixation;

import edu.ysu.itrace.filters.RawGaze;

public class Fixation {
	private int fixIndex;
	private RawGaze rawGaze;
	private long duration;
	
	public Fixation(int fixIndex, RawGaze rawGaze, long duration) {
		this.fixIndex = fixIndex;
		this.rawGaze = rawGaze;
		this.duration = duration;
	}
	
	public int getFixIndex() {
		return fixIndex;
	}
	
	public RawGaze getRawGaze() {
		return rawGaze;
	}
	
	public long getDuration() {
		return duration;
	}
}
