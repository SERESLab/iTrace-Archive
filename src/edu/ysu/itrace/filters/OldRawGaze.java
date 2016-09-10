package edu.ysu.itrace.filters;

/**
 * subclass for processing old raw gaze data.
 */
public class OldRawGaze extends RawGaze {

	private String hows;
	private String types; 
	private String fullyQualifiedNames;

	
	public OldRawGaze(String file, String type, double x, double y,
			double leftValidity, double rightValidity,
			double leftPupilDiam, double rightPupilDiam,
			long trackerTime, long systemTime, long nanoTime,
			int lineBaseX, int line, int col, String hows,
			String types, String fullyQualifiedNames, int lineBaseY) {
		
		super(file, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, trackerTime,
				systemTime, nanoTime, lineBaseX, line, col,
				lineBaseY);
		this.hows = hows;
		this.types = types;
		this.fullyQualifiedNames = fullyQualifiedNames;	
	}
	
	//Getters
	public String getHows() {
		return hows;
	}
	
	public String getTypes() {
		return types;
	}
	
	public String getFullyQualifiedNames() {
		return fullyQualifiedNames;
	}
}