package edu.ysu.itrace.filters;

import java.util.ArrayList;

/**
 * subclass for holding and processing new gaze data 
 * this would be currently exported data
 */
public class NewRawGaze extends RawGaze {
	private String path;
	private int lineHeight;
	private int fontHeight;
	private String timeStamp;
	private long sessionTime;
	
	private ArrayList<SourceCodeEntity> sces;
	
	public NewRawGaze(String name, String type, double x, double y,
			double leftValidity, double rightValidity,
			double leftPupilDiam, double rightPupilDiam, String timeStamp,
			long sessionTime, long trackerTime, long systemTime,
			long nanoTime, String path, int lineHeight, int fontHeight,
			int lineBaseX, int line, int col, int lineBaseY,
			ArrayList<SourceCodeEntity> sces) {
		
		super(name, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, trackerTime,
				systemTime, nanoTime, lineBaseX, line, col,
				lineBaseY);
		
		this.path = path;
		this.lineHeight = lineHeight;
		this.fontHeight = fontHeight;
		this.timeStamp = timeStamp;
		this.sessionTime = sessionTime;
		this.sces = sces;
	}
	
	//Getters
	public String getPath() {
		return path;
	}
	
	public int getLineHeight() {
		return lineHeight;
	}
	
	public int getFontHeight() {
		return fontHeight;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	
	public long getSessionTime() {
		return sessionTime;
	}
	
	public ArrayList<SourceCodeEntity> getSces() {
		return sces;
	}
}
