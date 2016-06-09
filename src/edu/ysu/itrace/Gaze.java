package edu.ysu.itrace;

import java.sql.Timestamp;
import java.util.Calendar;

public class Gaze {
    //Between 0.0 and 1.0
    private double x, left_x, right_x;
    private double y, left_y, right_y;
    //Between 0.0 to 1.0, where 0.0 is worst and 1.0 is best
    private double left_validity;
    private double right_validity;

    private double left_pupil_diameter;
    private double right_pupil_diameter;

    private long trackerTime;
    private Calendar calendar = Calendar.getInstance();
    private long nanoTime = System.nanoTime();
    private long systemTime = System.currentTimeMillis();
    private int nanoseconds;
    private Timestamp timestamp;
    private String timestampString;


    public Gaze(double left_x, double right_x, double left_y, double right_y,
                double left_validity, double right_validity,
                double left_pupil_diameter, double right_pupil_diameter,
                long trackerTime) {
        this.left_x = left_x;
        this.right_x = right_x;

        this.left_y = left_y;
        this.right_y = right_y;

        this.x = (left_x + right_x) / 2;
        this.y = (left_y + right_y) / 2;

        this.trackerTime = trackerTime;
        this.left_validity = left_validity;
        this.right_validity = right_validity;
        
        calendar.setTimeInMillis(systemTime);
        nanoseconds = (int) (nanoTime%1000000000);

        timestamp = new Timestamp(systemTime);
        timestamp.setNanos(nanoseconds);
        
        timestampString = timestamp.toString();
        timestampString = timestampString.substring(0, 10) + "T" + timestampString.substring(11);
        if(calendar.get(Calendar.ZONE_OFFSET) < 0) timestampString += "-";
        else timestampString += "+";
        if(Math.abs((calendar.get(Calendar.ZONE_OFFSET)/3600000)) < 10) timestampString += 0;
        timestampString += Math.abs((calendar.get(Calendar.ZONE_OFFSET)/3600000)) + ":00";
        
        this.left_pupil_diameter = left_pupil_diameter;
        this.right_pupil_diameter = right_pupil_diameter;
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

    public double getLeftPupilDiameter() {
        return left_pupil_diameter;
    }

    public double getRightPupilDiameter() {
        return right_pupil_diameter;
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
    public long getSessionTime(){
    	return nanoTime - Activator.getDefault().sessionStartTime;
    }
    public String getTimestamp(){
    	return timestampString;
    }
}
