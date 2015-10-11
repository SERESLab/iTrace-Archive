package edu.ysu.itrace.gaze.handlers;

import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.SOManager;
import edu.ysu.itrace.SOManager.StackOverflowEntity;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IStackOverflowGazeResponse;


/**
 * Implements the gaze handler interface for a Browser widget hosting a Stack Overflow question page.
 */
public class StackOverflowGazeHandler implements IGazeHandler {
	private IWorkbenchPartReference partRef;
	private Browser targetBrowser;

	    /**
	     * Constructs a new gaze handler for the target Browser stack overflow page object within the
	     * workbench part specified by partRef.
	     */
	    public StackOverflowGazeHandler(Object target, IWorkbenchPartReference partRef) {
	        this.targetBrowser = (Browser) target;
	        this.partRef = partRef;
	    }

	    @Override
	    public IStackOverflowGazeResponse handleGaze(int absoluteX, int absoluteY,
	            int relativeX, int relativeY, final Gaze gaze) {
	        final String name;
	        final SOManager.StackOverflowEntity entity;
	        final String url;
	        final String id;

	        /*
	         * If the browser is viewing a stack overflow question and answer page continue
	         * Otherwise the gaze is invalid, drop it
	         */
	        if (!targetBrowser.getUrl().contains("stackoverflow.com/questions/")) {
	            SOManager soManager = (SOManager) targetBrowser
	                    .getData(ControlView.KEY_SO_DOM);
	            
	            name = partRef.getPartName();
	            entity = soManager.getSOE(relativeX, relativeY);
	            /* If entity is null the gaze fell
	             * outside the valid text area, so just drop this one.
	             */
	            if (entity == null)
	            	return null;
	            url = soManager.getURL();
	            id = url.split(Pattern.quote("/"))[4];

	            /*
	             * This anonymous class just grabs the variables marked final
	             * in the enclosing method and returns them.
	             */
	            return new IStackOverflowGazeResponse() {
	            	@Override
	            	public String getName() {
	            		return name;
	            	}
	            	
	            	@Override
	            	public String getGazeType() {
	            		return "browser";
	            	}

	            	@Override
	            	public Gaze getGaze() {
	            		return gaze;
	            	}
	            	
	            	public IGazeHandler getGazeHandler() {
	            		return StackOverflowGazeHandler.this;
	            	}
	            	
	            	@Override
	            	public StackOverflowEntity getSOE() {
	            		return entity;
	            	}
	            	
	            	@Override
	            	public String getURL() {
	            		return url;
	            	}
	            	
	            	@Override
	            	public String getID() {
	            		return id;
	            	}
	            	
	            };
	        } else {
		    	return null;
	        } 
	    }
	}

