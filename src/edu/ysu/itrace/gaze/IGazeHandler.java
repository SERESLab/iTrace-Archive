package edu.ysu.itrace.gaze;

import edu.ysu.itrace.Gaze;



/**
 * Defines an object that can receive a point of gaze and respond in a
 * specialized way.
 */
public interface IGazeHandler {

    /**
     * Handles the specified gaze at the specified x and y coordinates relative
     * to the target object. Return value may be null if the gaze is not
     * meaningful to the target.
     */
    public IGazeResponse handleGaze(int x, int y, Gaze gaze);
}
