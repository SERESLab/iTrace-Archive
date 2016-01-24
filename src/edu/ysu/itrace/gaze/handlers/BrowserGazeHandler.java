package edu.ysu.itrace.gaze.handlers;

import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;

import edu.ysu.itrace.BRManager;
import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.SOManager;
import edu.ysu.itrace.BRManager.BugReportEntity;
import edu.ysu.itrace.SOManager.StackOverflowEntity;
import edu.ysu.itrace.gaze.IBugReportGazeResponse;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStackOverflowGazeResponse;


/**
 * Implements the gaze handler interface for a Browser widget hosting a Stack Overflow question page.
 */
public class BrowserGazeHandler implements IGazeHandler {
	private Browser targetBrowser;

	    /**e
	     * Constructs a new gaze handler for the target Browser SO/BR page object
	     */
	    public BrowserGazeHandler(Object target) {
	        this.targetBrowser = (Browser) target;
	    }

	    @Override
	    public IGazeResponse handleGaze(int absoluteX, int absoluteY,
	            int relativeX, int relativeY, final Gaze gaze) {
	        final String name;
	        final String url;
	        final String id;
	        final BRManager.BugReportEntity BRentity;
	        final SOManager.StackOverflowEntity SOentity;
	        
	        /*
	         * If the browser is viewing a stack overflow question and answer page continue
	         * If not, check if it is on a Bug Reports question
	         * Otherwise the gaze is invalid, drop it
	         */
	        if (targetBrowser.getUrl().contains("stackoverflow.com/questions/") &&
	        		 Character.isDigit(targetBrowser.getUrl().split(Pattern.quote("/"))[4].toCharArray()[0])) {
	        	SOManager soManager = (SOManager) targetBrowser
        				.getData(ControlView.KEY_SO_DOM);
        		
        		name = soManager.getTitle();
        		SOentity = soManager.getSOE(relativeX, relativeY);
        		/* If entity is null the gaze fell
        		 * outside the valid text area, so just drop this one.
        		 */
        		if (SOentity == null)
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
        				return BrowserGazeHandler.this;
        			}
        			
        			@Override
        			public StackOverflowEntity getSOE() {
        				return SOentity;
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
	        
	        }
	        
	        else if (targetBrowser.getUrl().contains("bugzilla.mozilla.org/show_bug.cgi?id=")) {
	        	
	        	BRManager brManager = (BRManager) targetBrowser
       				.getData(ControlView.KEY_BR_DOM);
	        	
       		name = brManager.getTitle();
       		BRentity = brManager.getBRE(relativeX, relativeY);
       		/* If entity is null the gaze fell
       		 * outside the valid text area, so just drop this one.
       		 */
       		if (BRentity == null)
       			return null;
       		url = brManager.getURL();
       		id = url.split(Pattern.quote("="))[1];
       		/*
       		 * This anonymous class just grabs the variables marked final
       		 * in the enclosing method and returns them.
       		 */
       		return new IBugReportGazeResponse() {
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
       				return BrowserGazeHandler.this;
       			}
       			
       			@Override
       			public BugReportEntity getBRE() {
       				return BRentity;
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
	        
	        }
	        
	        else {
		    	return null;
	        } 
	    }
	}

