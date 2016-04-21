package edu.ysu.itrace.gaze;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.draw2d.FigureCanvas;

import edu.ysu.itrace.gaze.handlers.ClassMLGazeHandler;
import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;

/**
 * Creates IGazeHandlers from objects within the Workbench.
 */
public class GazeHandlerFactory {

    /**
     * Creates and returns a new IGazeHandler object from the specified object,
     * or returns null if no handler object is defined for that object.
     */
    public static IGazeHandler createHandler(Object target,
            IWorkbenchPartReference partRef) {
        // create gaze handler for a StyledText widget
        if (target instanceof StyledText) {
            return new StyledTextGazeHandler(target);
        }
        
        // create gaze handler for a StyledText widget
        if (target instanceof FigureCanvas) {
        	System.out.println("#Instance of FigureCanvas in createHander()"); //TESTING
            return new ClassMLGazeHandler(target, partRef);
        }
        
        return null;
    }
}
