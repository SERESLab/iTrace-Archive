package edu.ysu.itrace.gaze;

import edu.ysu.itrace.SOManager.StackOverflowEntity;

/**
 * Defines an interface for gazes falling on Browser widgets hosting a Stack Overflow question page. 
 */
public interface IStackOverflowGazeResponse extends IGazeResponse {
	/**
	 * Returns a SOE, under the gaze
	 */
    public StackOverflowEntity getSOE();

    /**
     * Return the URL of the page in the browser
     */
    public String getURL();
    
    /**
     * Return the question ID of the Stack Overflow question
     */
    public String getID();
}
