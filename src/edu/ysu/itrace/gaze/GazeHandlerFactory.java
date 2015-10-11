package edu.ysu.itrace.gaze;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.gaze.handlers.StackOverflowGazeHandler;
import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;

/**
 * Creates IGazeHandlers from objects within a workbench part.
 */
public class GazeHandlerFactory {

    /**
     * Creates and returns a new IGazeHandler object from the specified object,
     * or returns null if no handler object is defined for that object.
     */
    public static IGazeHandler createHandler(Object target,
            IWorkbenchPartReference partRef) {
        // create gaze handler for a StyledText widget within an EditorPart
        if (target instanceof StyledText &&
                partRef instanceof IEditorReference) {
            return new StyledTextGazeHandler(target, partRef);
        }
        
        if (target instanceof Browser &&
        		partRef instanceof IEditorReference) {
        //create gaze handler for a Browser Stack overflow widget within an EditorPart
        	return new StackOverflowGazeHandler(target, partRef);
        }

        return null;
    }
}
