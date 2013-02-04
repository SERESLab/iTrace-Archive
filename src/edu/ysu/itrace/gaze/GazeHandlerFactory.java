package edu.ysu.itrace.gaze;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;
import edu.ysu.itrace.gaze.handlers.UmlFigureCanvasGazeHandler;
import edu.ysu.onionuml.ui.ModelViewer;

/**
 * Creates IGazeHandlers from objects within a workbench part.
 */
public class GazeHandlerFactory {
	
	/**
	 * Creates and returns a new IGazeHandler object from the specified object,
	 * or returns null if no handler object is defined for that object.
	 */
	public static IGazeHandler createHandler(Object target, IWorkbenchPartReference partRef){
		
		// create gaze handler for a StyledText widget within an EditorPart
		if(target instanceof StyledText && partRef instanceof IEditorReference){
			return new StyledTextGazeHandler(target, partRef);
		}
		
		// create gaze handler for a FigureCanvas widget within an editor for UML diagrams
		if(target instanceof FigureCanvas && partRef.getPart(false) instanceof ModelViewer){
			return new UmlFigureCanvasGazeHandler(target, partRef);
		}
		
		
		return null;
	}
}
