package edu.ysu.itrace.gaze;

import edu.ysu.itrace.BRManager.BugReportEntity;

/**
 * Defines an interface for gazes falling on Browser widgets hosting a Bug Report page. 
 */
public interface IBugReportGazeResponse extends IGazeResponse {
	/**
	 * Returns a BRE, under the gaze
	 */
    public BugReportEntity getBRE();

    /**
     * Return the URL of the page in the browser
     */
    public String getURL();
    
    /**
     * Return the question ID of the Bug Report
     */
    public String getID();
}
