package edu.ysu.itrace.gaze.handlers;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.ClassMLManager;
import edu.ysu.itrace.ClassMLManager.UMLEntity;
import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IClassMLGazeResponse;
import edu.ysu.itrace.gaze.IGazeHandler;

public class ClassMLGazeHandler implements IGazeHandler{
	private IWorkbenchPartReference partRef;
	private FigureCanvas targetFigureCanvas;

	    /**
	     * Constructs a new gaze handler for the target UML FigureCanvas object within the
	     * workbench part specified by partRef.
	     */
	    public ClassMLGazeHandler(Object target, IWorkbenchPartReference partRef) {
	    	this.targetFigureCanvas = (FigureCanvas) target;
	        this.partRef = partRef;
	    }

	    @Override
	    public IClassMLGazeResponse handleGaze(int absoluteX, int absoluteY,
	            int relativeX, int relativeY, final Gaze gaze){
	    	
	    	final String name;
	        final ClassMLManager.UMLEntity entity;
	        
	        //SOMETHING CURRENTLY HAPPENING HERE THAT IS NOT WORKING
	    	ClassMLManager classMLManager = (ClassMLManager) targetFigureCanvas
	    			.getData(ControlView.KEY_CLASSML_DOM);  
	    	
	    	name = partRef.getPage().getActiveEditor().getTitle();
	    	entity = classMLManager.getUMLE(relativeX, relativeY);	//This will currently throw a null pointer exception
	    	
	    	/* If entity is null the gaze fell
    		 * outside the valid text area, so just drop this one.
    		 */
    		if (entity == null)
    			return null;	
	    	
    		/*
    		 * This anonymous class just grabs the variables marked final
    		 * in the enclosing method and returns them.
    		 */
    		return new IClassMLGazeResponse() {
    			@Override
    			public String getName() {
    				return name;
    			}
    			
    			@Override
    			public String getGazeType() {
    				return "diagram";
    			}
    			
    			@Override
    			public Gaze getGaze() {
    				return gaze;
    			}
    			
    			public IGazeHandler getGazeHandler() {
    				return ClassMLGazeHandler.this;
    			}
    			
    			@Override
    			public UMLEntity getUMLE() {
    				return entity;
    			}

    		};		
    		
	    }
}