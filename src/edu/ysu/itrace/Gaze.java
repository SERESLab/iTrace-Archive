package edu.ysu.itrace;

import java.util.Date;

import edu.ysu.itrace.gaze.IGazeResponse;

public class Gaze {
	//Between 0.0 and 1.0
	private double x;
	private double y;
	//Between 0.0 to 1.0, where 0.0 is worst and 1.0 is best
	private double left_validity;
	private double right_validity;

	private Date timeStamp;
	private IGazeResponse response;
	
	public Gaze(double x, double y, double left_validity, double right_validity, 
			Date timestamp) {
		this.x = x;
		this.y = y;
		this.timeStamp = timestamp;
		this.left_validity = left_validity;
		this.right_validity = right_validity;
	}
	
	public Gaze(double x, double y, double left_validity, double right_validity, 
			Date timestamp, IGazeResponse response) {
		this.x = x;
		this.y = y;
		this.timeStamp = timestamp;
		this.response = response;
		this.left_validity = left_validity;
		this.right_validity = right_validity;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getLeftValidity() {
		return left_validity;
	}

	public double getRightValidity() {
		return right_validity;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}
}
