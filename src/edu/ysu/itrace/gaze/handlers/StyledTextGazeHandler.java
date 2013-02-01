package edu.ysu.itrace.gaze.handlers;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;


/**
 * Implements the gaze handler interface for a StyledText widget.
 */
public class StyledTextGazeHandler implements IGazeHandler {

	private IWorkbenchPartReference partRef;
	private StyledText targetStyledText;
	
	/**
	 * Constructs a new gaze handler for the target StyledText object within
	 * the workbench part specified by partRef.
	 */
	public StyledTextGazeHandler(Object target, IWorkbenchPartReference partRef){
		assert(target instanceof StyledText);
		this.targetStyledText = (StyledText)target;
		this.partRef = partRef;
	}
	
	
	@Override
	public IGazeResponse handleGaze(final int x, final int y) {
		
		return new IGazeResponse(){
			@Override
			public String toLogString() {
				int lineIndex = targetStyledText.getLineIndex(y);
				return partRef.getPartName() + " at (" + x + ", " + y + "): "
						+ targetStyledText.getLine(lineIndex);
			}
		};
	}

}
