package edu.ysu.itrace.gaze;

import org.eclipse.ui.IWorkbenchPartReference;


/**
 * Defines an object inside a workbench part that can receive a
 * point of gaze and respond in a specialized way.
 */
public interface IGazeHandler {

	/**
	 * Handles the gaze at the specified x and y coordinates relative
	 * to the target object.
	 */
	public IGazeResponse handleGaze(int x, int y, Object target);
	
	/**
	 * Receives the reference to the workbench part to which the object belongs.
	 */
	public void setPartReference(IWorkbenchPartReference partRef);
}
