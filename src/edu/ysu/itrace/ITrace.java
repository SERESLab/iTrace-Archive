package edu.ysu.itrace;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;
import edu.ysu.itrace.preferences.PluginPreferences;
import edu.ysu.itrace.solvers.sessionserver.ISessionTimeServer;
import edu.ysu.itrace.solvers.sessionserver.SessionTimeServer;
import edu.ysu.itrace.solvers.windowfocus.IWindowFocusTracker;
import edu.ysu.itrace.solvers.windowfocus.WindowFocusTracker;
import edu.ysu.itrace.solvers.ISolver;
import edu.ysu.itrace.solvers.JSONGazeExportSolver;
import edu.ysu.itrace.solvers.XMLGazeExportSolver;
import edu.ysu.itrace.solvers.emotionpopup.EmotionPopupHandler;
import edu.ysu.itrace.solvers.emotionpopup.IEmotionPopupHandler;
import edu.ysu.itrace.solvers.externallauncher.ExternalLauncher;
import edu.ysu.itrace.solvers.externallauncher.IExternalLauncher;
import edu.ysu.itrace.solvers.keytracking.IKeyTrackingSolver;
import edu.ysu.itrace.solvers.keytracking.KeyTrackingSolver;
import edu.ysu.itrace.trackers.IEyeTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class ITrace extends AbstractUIPlugin implements EventHandler {

    // The plug-in ID
    public static final String PLUGIN_ID = "edu.ysu.itrace"; //$NON-NLS-1$
    public long sessionStartTime;
    public Rectangle monitorBounds;
    // The shared instance
    private static ITrace plugin;
    private IEditorPart activeEditor;
    private HashMap<IEditorPart,TokenHighlighter> tokenHighlighters = new HashMap<IEditorPart,TokenHighlighter>();
    private boolean showTokenHighlights = false;
    
    private IEyeTracker tracker = null;
    private volatile boolean recording;
    private JSONGazeExportSolver jsonSolver;
    private XMLGazeExportSolver xmlSolver;
    private boolean jsonOutput = true;
    private boolean xmlOutput = true;
    
    private IActionBars actionBars;
    private IStatusLineManager statusLineManager;
    private long registerTime = 2000;
    private IEventBroker eventBroker;
    private SessionInfoHandler sessionInfo = new SessionInfoHandler();
    
    private Shell rootShell;
    
    private ISessionTimeServer sessionTimeServer;
    private IEmotionPopupHandler emotionPopupHandler;
    private IExternalLauncher externalLauncher;
    private IKeyTrackingSolver keyTracker;
    private IWindowFocusTracker windowFocusTracker;
    
    /**
     * The constructor
     */
    public ITrace() {
    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    	/*
    	if(editorPart != null){
	    	StyledText styledText = (StyledText) editorPart.getAdapter(Control.class);
	    	if(styledText != null){
				ITextOperationTarget t = (ITextOperationTarget) activeEditor.getAdapter(ITextOperationTarget.class);
				if(t instanceof ProjectionViewer){
					ProjectionViewer projectionViewer = (ProjectionViewer)t;
					tokenHighlighters.put(activeEditor, new TokenHighlighter(styledText, showTokenHighlights, projectionViewer));
				}
			}
    	}
    	*/
    	eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
    	eventBroker.subscribe("iTrace/newgaze", this);
    	jsonSolver = new JSONGazeExportSolver();
    	xmlSolver = new XMLGazeExportSolver();
        sessionTimeServer = new SessionTimeServer();
        emotionPopupHandler = new EmotionPopupHandler();
        externalLauncher = new ExternalLauncher();
        keyTracker = new KeyTrackingSolver();
        windowFocusTracker = new WindowFocusTracker();
    	eventBroker.subscribe("iTrace/jsonOutput", jsonSolver);
    	eventBroker.subscribe("iTrace/xmlOutput", xmlSolver);
        eventBroker.subscribe("iTrace/sessionTimeServer", (SessionTimeServer) sessionTimeServer);
        eventBroker.subscribe("iTrace/emotionPopup", (EmotionPopupHandler) emotionPopupHandler);
        eventBroker.subscribe("iTrace/externalLauncher", (ExternalLauncher) externalLauncher);
        eventBroker.subscribe("iTrace/keyTracker", (KeyTrackingSolver) keyTracker);
        eventBroker.subscribe("iTrace/windowFocusTracker", (WindowFocusTracker) windowFocusTracker);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        IPreferenceStore prefStore = getDefault().getPreferenceStore();
        EyeTrackerFactory.setTrackerType(EyeTrackerFactory.TrackerType.valueOf(
                prefStore.getString(PluginPreferences.EYE_TRACKER_TYPE)));
        activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	if (recording) {
            stopTracking();
        }
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ITrace getDefault() {
        return plugin;
    }
    
    public IEyeTracker getTracker(){
    	return tracker;
    }
    
    public void setTrackerXDrift(int drift){
    	tracker.setXDrift(drift);
    }
    public void setTrackerYDrift(int drift){
    	tracker.setYDrift(drift);
    }
    
    public Shell getRootShell(){
    	return rootShell;
    }
    public void setRootShell(Shell shell){
    	rootShell = shell;
    }
    
    public void setActionBars(IActionBars bars){
    	actionBars = bars;
    	statusLineManager = actionBars.getStatusLineManager();
    }
    
    public void setLineManager(IStatusLineManager manager){
    	statusLineManager = manager;
    }
    
    public void setJsonOutput(boolean value){
    	jsonOutput = value;
    }
    public void displayJsonExportFile(){
    	jsonSolver.displayExportFile();
    }
    
    public void setXmlOutput(boolean value){
    	xmlOutput = value;
    }
    public void displayXmlExportFile(){
    	xmlSolver.displayExportFile();
    }
    
    public boolean sessionInfoConfigured(){
    	return sessionInfo.isConfigured();
    }
    
    public void calibrateTracker(){
    	requestTracker();
        if (tracker != null) {
            try {
                tracker.calibrate();
            } catch (CalibrationException e1) {
            	eventBroker.post("iTrace/error", e1.getMessage());
            }
        } else {
            // If tracker is none, requestTracker() would have already
            // raised an error.
        }
    }
    
    public boolean startTracking() {
        if (recording) {
            eventBroker.post("iTrace/error", "Tracking is already in progress.");
            return recording;
        }
        if (!requestTracker()) {
            // Error handling occurs in requestTracker(). Just return and
            // pretend
            // nothing happened.
            return recording;
        }
        //eventBroker.subscribe("iTrace/newgaze", this);
        
        if (!sessionInfo.isConfigured()) {
        	eventBroker.post("iTrace/error", "You have not configured your Session Info.");
        	return recording;
        }
        
        try {
        	sessionInfo.export();
        } catch(IOException e) {
        	System.out.println(e.getMessage());
        }
        ITrace.getDefault().sessionStartTime = System.nanoTime();
        jsonSolver.init();
        xmlSolver.init();
        sessionTimeServer.init();
        emotionPopupHandler.init();
        externalLauncher.init();
        keyTracker.init();
        windowFocusTracker.init();
        recording = true;
        return recording;
    }
    
    public boolean stopTracking() {
        if (!recording) {
        	eventBroker.post("iTrace/error", "Tracking is not in progress.");
            return false;
        }
        sessionInfo.reset();
        
        xmlSolver.dispose();
        jsonSolver.dispose();
        sessionTimeServer.dispose();
        emotionPopupHandler.dispose();
        externalLauncher.dispose();
        keyTracker.dispose();
        windowFocusTracker.dispose();
        
        if (tracker != null) {
        } else {
            // If there is no tracker, tracking should not be occurring anyways.
            System.out.println("No Tracker");
        }
        statusLineManager.setMessage("");

        recording = false;
        return true;
    }
    
    public boolean toggleTracking(){
    	if(recording) return stopTracking();
    	else return startTracking();
    }
    
    public boolean displayCrosshair(boolean display){
    	if (tracker == null)
            requestTracker();
        if (tracker != null) {
        	tracker.displayCrosshair(display);
        	return display;
        }
       	return !display;
    }
    
    public void configureSessionInfo(){
    	sessionInfo.config();
    	if (sessionInfo.isConfigured()) {
    		xmlSolver.config(sessionInfo.getSessionID(),
    				sessionInfo.getDevUsername());
    		jsonSolver.config(sessionInfo.getSessionID(),
    				sessionInfo.getDevUsername());
    		sessionTimeServer.config(sessionInfo.getSessionID(),
    				sessionInfo.getDevUsername());
    		emotionPopupHandler.config(sessionInfo.getSessionID(),
    				sessionInfo.getDevUsername());
    		externalLauncher.config(sessionInfo.getSessionID(), 
    				sessionInfo.getDevUsername());
    		keyTracker.config(sessionInfo.getSessionID(), 
    				sessionInfo.getDevUsername());
    		windowFocusTracker.config(sessionInfo.getSessionID(), 
    				sessionInfo.getDevUsername());
    	}
    }
    
    public void setActiveEditor(IEditorPart editorPart){
    	activeEditor = editorPart;
    	if(activeEditor == null) return;
    	if(!tokenHighlighters.containsKey(editorPart)){
    		StyledText styledText = (StyledText) editorPart.getAdapter(Control.class);
    		if(styledText != null){
				ITextOperationTarget t = (ITextOperationTarget) activeEditor.getAdapter(ITextOperationTarget.class);
				if(t instanceof ProjectionViewer){
					ProjectionViewer projectionViewer = (ProjectionViewer)t;
					tokenHighlighters.put(activeEditor, new TokenHighlighter(styledText, showTokenHighlights, projectionViewer));
				}
			}
    	}
    	
    }
    
    public void displayEyeStatus(){
    	if (tracker == null)
            requestTracker();
        if (tracker != null){
        	EyeStatusWindow statusWindow = new EyeStatusWindow();
        	statusWindow.setVisible(true);
        }
    }
    
    public void activateHighlights(){
    	if (tracker == null)
            requestTracker();
		
        
        if (tracker != null){
        	showTokenHighLights();
        }
    }
    
    public void removeHighlighter(IEditorPart editorPart){
    	tokenHighlighters.remove(editorPart);
    }
    
    public void showTokenHighLights(){
    	showTokenHighlights = !showTokenHighlights;
    	if(activeEditor == null) return;
		if(!tokenHighlighters.containsKey(activeEditor)){
			StyledText styledText = (StyledText) activeEditor.getAdapter(Control.class);
			if(styledText != null){
				ITextOperationTarget t = (ITextOperationTarget) activeEditor.getAdapter(ITextOperationTarget.class);
				if(t instanceof ProjectionViewer){
					ProjectionViewer projectionViewer = (ProjectionViewer)t;
					tokenHighlighters.put(activeEditor, new TokenHighlighter(styledText, showTokenHighlights, projectionViewer));
				}
			}
		}
    	for(TokenHighlighter tokenHighlighter: tokenHighlighters.values()){
    		tokenHighlighter.setShow(showTokenHighlights);
    	}
    }
    
    private boolean requestTracker() {
        if (tracker != null) {
            // Already have a tracker. Don't need another.
            return true;
        }
        try {
            tracker = EyeTrackerFactory.getConcreteEyeTracker();
            if (tracker != null) {
            	tracker.startTracking();
                return true;
            }else{
            	return false;
            }
        } catch (EyeTrackerConnectException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Finds the control under the specified screen coordinates and calls its
     * gaze handler on the localized point. Returns the gaze response or null if
     * the gaze is not handled.
     */
    private IGazeResponse handleGaze(int screenX, int screenY, Gaze gaze){
    	Queue<Control[]> childrenQueue = new LinkedList<Control[]>();
    	childrenQueue.add(rootShell.getChildren());
    	Rectangle monitorBounds = rootShell.getMonitor().getBounds();
        while (!childrenQueue.isEmpty()) {
            for (Control child : childrenQueue.remove()) {
                Rectangle childScreenBounds = child.getBounds();
                Point screenPos = child.toDisplay(0, 0);
                childScreenBounds.x = screenPos.x - monitorBounds.x;
                childScreenBounds.y = screenPos.y - monitorBounds.y;
                if (childScreenBounds.contains(screenX, screenY)) {
                    if (child instanceof Composite) {
                        Control[] nextChildren =
                                ((Composite) child).getChildren();
                        if (nextChildren.length > 0 && nextChildren[0] != null) {
                            childrenQueue.add(nextChildren);
                        }
                    }
                    IGazeHandler handler =
                            (IGazeHandler) child
                                    .getData(HandlerBindManager.KEY_HANDLER);
                    if (child.isVisible() && handler != null) {
                        return handler.handleGaze(screenX, screenY,
                                screenX - childScreenBounds.x, screenY
                                        - childScreenBounds.y, gaze);
                    }
                }
            }
        }
        return null;
    }
    
    @Override
	public void handleEvent(Event event) {
		if(event.getTopic() == "iTrace/newgaze"){
			String[] propertyNames = event.getPropertyNames();
			Gaze g = (Gaze)event.getProperty(propertyNames[0]);
			 if (g != null) {
	             if(!rootShell.isDisposed()){
	            	 Rectangle monitorBounds = rootShell.getMonitor().getBounds();
	            	 int screenX = (int) (g.getX() * monitorBounds.width);
		             int screenY = (int) (g.getY() * monitorBounds.height);
		             IGazeResponse response;
	            	 response = handleGaze(screenX, screenY, g);
	            	 
	            	 if (response != null) {
		                	 if(recording){
		                		 statusLineManager
		                 			.setMessage(String.valueOf(response.getGaze().getSessionTime()));
		                 		registerTime = System.currentTimeMillis();
		                 		if(xmlOutput) eventBroker.post("iTrace/xmlOutput", response);
		                 		if(jsonOutput) eventBroker.post("iTrace/jsonOutput", response);
                                eventBroker.post("iTrace/sessionTimeServer", response);
                                eventBroker.post("iTrace/emotionPopup", response);
                                eventBroker.post("iTrace/keyTracker", response);
                                eventBroker.post("iTrace/windowFocusTracker", response);
		                	 }
		                     
		                     if(response instanceof IStyledTextGazeResponse && response != null && showTokenHighlights){
		                     	IStyledTextGazeResponse styledTextResponse = (IStyledTextGazeResponse)response;
		                     	eventBroker.post("iTrace/newstresponse", styledTextResponse);
		                     }
		             }
		         }else{
		         	if((System.currentTimeMillis()-registerTime) > 2000){
		         		statusLineManager.setMessage("");
		         	}
		         }
	         }
		}
	}
}
