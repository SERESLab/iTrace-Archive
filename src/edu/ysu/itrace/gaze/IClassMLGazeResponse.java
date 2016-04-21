package edu.ysu.itrace.gaze;

import edu.ysu.itrace.ClassMLManager.UMLEntity;

/**
 * Defines an interface for gazes falling on entities in a GraphicalEditor 
 */
public interface IClassMLGazeResponse extends IGazeResponse{
	/**
	 * Returns a UMLE, under the gaze
	 */
    public UMLEntity getUMLE();

}
