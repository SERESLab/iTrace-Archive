package edu.ysu.itrace;

import java.util.Date;

import edu.ysu.itrace.gaze.IGazeResponse;

public class Gaze {
    //Between 0.0 and 1.0
    private double x, left_x, right_x;
    private double y, left_y, right_y;
    //Between 0.0 to 1.0, where 0.0 is worst and 1.0 is best
    private double left_validity;
    private double right_validity;

    private Date timeStamp;

    public Gaze(double left_x, double right_x, double left_y, double right_y,
                double left_validity, double right_validity, Date timestamp) {
        this.left_x = left_x;
        this.right_x = right_x;

        this.left_y = left_y;
        this.right_y = right_y;

        this.x = (left_x + right_x) / 2;
        this.y = (left_y + right_y) / 2;

        this.timeStamp = timestamp;
        this.left_validity = left_validity;
        this.right_validity = right_validity;
    }

    public double getX() {
        return x;
    }

    public double getLeftX() {
        return left_x;
    }

    public double getRightX() {
        return right_x;
    }

    public double getY() {
        return y;
    }

    public double getLeftY() {
        return left_y;
    }

    public double getRightY() {
        return right_y;
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
