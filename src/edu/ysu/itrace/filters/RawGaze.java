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
	private int trackerTime;
	private int systemTime;
	private int nanoTime;
	private int lineBaseX;
	private int line;
	private int col;
	private String hows;
	private String types; 
	private String fullyQualifiedNames;
	private int lineBaseY;
	
	public RawGaze(String file, String type, double x, double y,
			double leftValidity, double rightValidity,
			double leftPupilDiam, double rightPupilDiam,
			int trackerTime, int systemTime, int nanoTime,
			int lineBaseX, int line, int col, String hows,
			String types, String fullyQualifiedNames, int lineBaseY) {
		
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
		this.hows = hows;
		this.types = types;
		this.fullyQualifiedNames = fullyQualifiedNames;	
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
	
	public int getTrackerTime() {
		return trackerTime;
	}
	
	public int getSystemTime() {
		return systemTime;
	}
	
	public int getNanoTime() {
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
	
	public String getHows() {
		return hows;
	}
	
	public String getTypes() {
		return types;
	}
	
	public String getFullyQualifiedNames() {
		return fullyQualifiedNames;
	}
	
	public int getLineBaseY() {
		return lineBaseY;
	}
}