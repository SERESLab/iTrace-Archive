package edu.ysu.itrace;

import java.util.Date;

public class Gaze {
	private int x;
	private int y;
	private int length;
	private Date timeStamp;
	
	public Gaze(int x, int y, int length) {
		this.x = x;
		this.y = y;
		this.length = length;
		
		this.timeStamp = new Date();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getGazeLength() {
		return length;
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}
}
