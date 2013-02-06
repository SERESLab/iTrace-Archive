package edu.ysu.itrace;

import java.util.Date;

public class Gaze {
	private double x; //Between 0.0 and 1.0
	private double y; //Between 0.0 and 1.0
	private Date timeStamp;

	public Gaze(double x, double y, Date timestamp) {
		this.x = x;
		this.y = y;
		this.timeStamp = timestamp;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}
}
