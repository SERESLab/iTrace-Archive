package edu.ysu.itrace.gaze;

/**
 * Defines a response to a gaze event. Returned by objects implementing IGazeHandler.
 */
public interface IGazeResponse {

	/**
	 * Returns a formatted string that can be written a log file.
	 */
	public String toLogString();
}
