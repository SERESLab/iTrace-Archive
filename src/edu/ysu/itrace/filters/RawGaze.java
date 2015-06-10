package edu.ysu.itrace.filters;

/**
 * Class that holds raw gaze data information. 
 * It may be re-inventing the wheel.
 */
public class RawGaze {
	private String file;
	private String type;
	private double x;
	private double y;
	private double leftValidity;
	private double rightValidity;
	private double leftPupilDiam;
	private double rightPupilDiam;
	private long trackerTime;
	private long systemTime;
	private long nanoTime;
	
	private int lineBaseX;
	private int line;
	private int col;
	private int lineBaseY;
	
	public RawGaze(String file, String type, double x, double y,
			double leftValidity, double rightValidity,
			double leftPupilDiam, double rightPupilDiam,
			long trackerTime, long systemTime, long nanoTime,
			int lineBaseX, int line, int col, int lineBaseY) {

		this.file = file;
		this.type = type;
		this.x = x;
		this.y = y;
		this.leftValidity = leftValidity;
		this.rightValidity = rightValidity;
		this.leftPupilDiam = leftPupilDiam;
		this.rightPupilDiam = rightPupilDiam;
		this.trackerTime = trackerTime;
		this.systemTime = systemTime;
		this.nanoTime = nanoTime;
		this.lineBaseX = lineBaseX;
		this.lineBaseY = lineBaseY;
		this.line = line;
		this.col = col;
	}
	
	//Getters
	public String getFile() {
		return file;
	}
	
	public String getType() {
		return type;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getLeftValid() {
		return leftValidity;
	}
	
	public double getRightValid() {
		return rightValidity;
	}
	
	public double getLeftPupilDiam() {
		return leftPupilDiam;
	}
	
	public double getRightPupilDiam() {
		return rightPupilDiam;
	}
	
	public long getTrackerTime() {
		return trackerTime;
	}
	
	public long getSystemTime() {
		return systemTime;
	}
	
	public long getNanoTime() {
		return nanoTime;
	}
	
	public int getLineBaseX() {
		return lineBaseX;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getCol() {
		return col;
	}
	
	public int getLineBaseY() {
		return lineBaseY;
	}
}
