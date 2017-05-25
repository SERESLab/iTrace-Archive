package edu.ysu.itrace.filters.fixation;

import edu.ysu.itrace.filters.RawGaze;
import edu.ysu.itrace.filters.exceptions.FixationException;

public class Fixation {
	private int fixIndex;
	private RawGaze rawGaze;
	private long duration;
	
	public Fixation(int fixIndex, RawGaze rawGaze, long duration) {
		this.fixIndex = fixIndex;
		this.rawGaze = rawGaze;
		this.duration = duration;
	}
	
	//Getters
	public int getFixIndex() {
		return fixIndex;
	}
	
	public RawGaze getRawGaze() {
		return rawGaze;
	}
	
	public long getDuration() {
		return duration;
	}
	
	//Setters
	public void setFixIndex(int newIndex) throws FixationException {
		if (newIndex >= 1) {
			this.fixIndex = newIndex;
		} else {
			throw new FixationException("Fixation indices must be >= 1.");
		}
	}
}
