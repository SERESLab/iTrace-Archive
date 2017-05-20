package edu.ysu.itrace.filters.fixation;

import java.util.ArrayList;

import edu.ysu.itrace.filters.NewRawGaze;
import edu.ysu.itrace.filters.SourceCodeEntity;

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
}
