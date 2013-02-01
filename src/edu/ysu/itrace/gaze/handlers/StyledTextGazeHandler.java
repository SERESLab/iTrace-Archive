package edu.ysu.itrace.gaze.handlers;

import org.eclipse.swt.custom.StyledText;

import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;


/**
 * Implements the gaze handler interface for a StyledText widget.
 */
public class StyledTextGazeHandler implements IGazeHandler {

	
	@Override
	public IGazeResponse handleGaze(final int x, final int y, Object target) {
		assert(target instanceof StyledText);
		
		final StyledText st = (StyledText)target;
		
		return new IGazeResponse(){
			@Override
			public String toLogString() {
				int lineIndex = st.getLineIndex(y);
				return "Line at (" + x + ", " + y + "): " + st.getLine(lineIndex);
			}
			
		};
	}

}
