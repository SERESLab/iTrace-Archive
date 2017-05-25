package edu.ysu.itrace.filters.fixation;

import java.util.ArrayList;

import edu.ysu.itrace.filters.NewRawGaze;
import edu.ysu.itrace.filters.SourceCodeEntity;
import edu.ysu.itrace.filters.exceptions.RawGazeException;

public class NewRawGazeFixation extends NewRawGaze {
	private int fixIndex;
	private double fixX;
	private double fixY;
	private long duration;
	
	public NewRawGazeFixation(String name, String type, double x, double y,
			double leftValidity, double rightValidity,
			double leftPupilDiam, double rightPupilDiam, String timeStamp,
			long sessionTime, long trackerTime, long systemTime,
			long nanoTime, String path, int lineHeight, int fontHeight,
			int lineBaseX, int line, int col, int lineBaseY,
			int fixIndex, double fixX, double fixY, long duration,
			ArrayList<SourceCodeEntity> sces) {
		
		super(name, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, timeStamp,
				sessionTime, trackerTime, systemTime,
				nanoTime, path, lineHeight, fontHeight,
				lineBaseX, line, col, lineBaseY, sces);

		this.fixIndex = fixIndex;
		this.fixX = fixX;
		this.fixY = fixY;
		this.duration = duration;
	}

	//Getters
	public int getFixIndex() {
		return fixIndex;
	}
	
	public double getFixX() {
		return fixX;
	}
	
	public double getFixY() {
		return fixY;
	}
	
	public long getDuration() {
		return duration;
	}
	
	//Setters
	public void setFixIndex(int newIndex) throws RawGazeException {
		if (newIndex >= 1 || newIndex == -1) {
			this.fixIndex = newIndex;
		} else {
			throw new RawGazeException("Raw gaze associated fixation indices must be >= 1 or = -1.");
		}
	}
	
	public void setFixX(double x) { //should really check that it is within the screen bounds from data or -1
		this.fixX = x;
	}
	
	public void setFixY(double y) { //should really check that it is within the screen bounds from data or -1
		this.fixY = y;
	}
	
	public void setDuration(long newDuration) throws RawGazeException {
		if (newDuration >= 0 || newDuration == -1) {
			this.duration = newDuration;
		} else {
			throw new RawGazeException("Raw gaze associated fixation duration must be non-negative or -1.");
		}
	}
}
