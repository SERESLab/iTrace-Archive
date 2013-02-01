package edu.ysu.itrace.gaze;

import org.eclipse.swt.custom.StyledText;

import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;

/**
 * Creates IGazeHandlers from objects.
 */
public class GazeHandlerFactory {
	
	/**
	 * Creates and returns a new IGazeHandler object from the specified object,
	 * or returns null if no handler object is defined for that object.
	 */
	public static IGazeHandler createHandler(Object o){
		
		if(o instanceof StyledText){
			return new StyledTextGazeHandler();
		}
		
		return null;
	}
}
