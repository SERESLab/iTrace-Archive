package edu.ysu.itrace.gaze;



/**
 * Defines an object that can receive a point of gaze and respond in a specialized way.
 */
public interface IGazeHandler {

	/**
	 * Handles the gaze at the specified x and y coordinates relative
	 * to the target object.
	 */
	public IGazeResponse handleGaze(int x, int y);
}
