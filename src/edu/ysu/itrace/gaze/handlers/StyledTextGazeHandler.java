package edu.ysu.itrace.gaze.handlers;

import java.util.Hashtable;
import java.util.Map;

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
		
		IGazeResponse response = new IGazeResponse(){
			
			private String name = partRef.getPartName();
			private String type = null;
			private Map<String,String> properties = new Hashtable<String,String>();
			
			// construct the type and properties for the response
			{
				int index = targetStyledText.getLineIndex(y);
				String line = targetStyledText.getLine(index).trim();
				
				if(line.length() > 0){
					this.type = "source_code";
					this.properties.put("gaze_line", line);
				}
			}
			
			@Override
			public String getName() {
				return this.name;
			}
			
			@Override
			public String getType() {
				return this.type;
			}
			
			@Override
			public Map<String, String> getProperties() {
				return this.properties;
			}
		};
		
		return (response.getType() != null ? response : null);
	}

}
