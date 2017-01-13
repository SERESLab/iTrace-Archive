package edu.ysu.itrace;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.e4.core.services.events.IEventBroker;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;
import edu.ysu.itrace.filters.IFilter;
import edu.ysu.itrace.filters.fixation.JSONBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.OldJSONBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.OldXMLBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.XMLBasicFixationFilter;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;
import edu.ysu.itrace.solvers.ISolver;
import edu.ysu.itrace.solvers.JSONGazeExportSolver;
import edu.ysu.itrace.solvers.XMLGazeExportSolver;
import edu.ysu.itrace.trackers.IEyeTracker;

/**
 * ViewPart for managing and controlling the plugin.
 */
public class ControlView extends ViewPart implements IPartListener2, EventHandler{
    private static final int POLL_GAZES_MS = 5;
    public static final String KEY_AST = "itraceAST";
    public static final String KEY_SO_DOM = "itraceSO";
    public static final String KEY_BR_DOM = "itraceBR";
    public static final String FATAL_ERROR_MSG = "A fatal error occurred. "
            + "Restart the plugin and try again. If "
            + "the problem persists, submit a bug report.";

    private IEyeTracker tracker = null;
    private Shell rootShell;

    private CopyOnWriteArrayList<Control> grayedControls =
            new CopyOnWriteArrayList<Control>();
    
    private LinkedBlockingQueue<Gaze> standardTrackingQueue = null;
    private LinkedBlockingQueue<Gaze> crosshairQueue = null;

    private volatile boolean trackingInProgress;
    private LinkedBlockingQueue<IGazeResponse> gazeResponses =
            new LinkedBlockingQueue<IGazeResponse>();

    private Spinner xDrift;
    private Spinner yDrift;

    private JSONGazeExportSolver jsonSolver;
    private XMLGazeExportSolver xmlSolver;

    private CopyOnWriteArrayList<ISolver> availableSolvers =
            new CopyOnWriteArrayList<ISolver>();

    private CopyOnWriteArrayList<ISolver> activeSolvers =
            new CopyOnWriteArrayList<ISolver>();
    
    private CopyOnWriteArrayList<IFilter> availableFilters =
    		new CopyOnWriteArrayList<IFilter>();
    
    private SessionInfoHandler sessionInfo = new SessionInfoHandler();
    
    private IActionBars actionBars;
    private IStatusLineManager statusLineManager;
    private long registerTime = 2000;
    private IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);

    @Override
    public void createPartControl(Composite parent) {
        // find root shell
        rootShell = parent.getShell();
        while (rootShell.getParent() != null) {
            rootShell = rootShell.getParent().getShell();
        }
        Activator.getDefault().monitorBounds = rootShell.getMonitor().getBounds();

        // add listener for determining part visibility
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

        final String DONT_DO_THAT_MSG =
                "You can't do that until you've "
                        + "selected a tracker in preferences.";

        // set up UI
        parent.setLayout(new RowLayout());

        final Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));

        Button calibrateButton = new Button(buttonComposite, SWT.PUSH);
        calibrateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
        calibrateButton.setText("Calibrate");
        calibrateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                requestTracker();
                if (tracker != null) {
                    try {
                        tracker.calibrate();
                    } catch (CalibrationException e1) {
                        displayError("Failed to calibrate. Reason: "
                                + e1.getMessage());
                    }
                } else {
                    // If tracker is none, requestTracker() would have already
                    // raised an error.
                }
            }
        });
        
        final Button startButton = new Button(buttonComposite, SWT.PUSH);
        startButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        startButton.setText("Start Tracking");
        startButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	actionBars = getViewSite().getActionBars();
            	statusLineManager = actionBars.getStatusLineManager();
                startTracking();
            }
        });
        
        /*
        final Button displayStatus = new Button(buttonComposite, SWT.PUSH);
        displayStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
        displayStatus.setText("Display Status");
        displayStatus.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (gazeTransport != null) {
                    (new EyeStatusView(rootShell, gazeTransport)).open();
                } else {
                    displayError(DONT_DO_THAT_MSG);
                }
            }
        });*/

        final String DONT_CHANGE_THAT_MSG =
                "Don't change this value until "
                        + "you've selected a tracker in preferences.";

        final Composite tuningComposite = new Composite(parent, SWT.NONE);
        tuningComposite.setLayout(new RowLayout(SWT.VERTICAL));

        final Button highlight_tokens = new Button(tuningComposite, SWT.CHECK);
        highlight_tokens.setText("Highlight Tokens");
        highlight_tokens.addSelectionListener(new SelectionAdapter(){
        	@Override
            public void widgetSelected(SelectionEvent e) {
        		activateHighlights();
        	}
        });
        
        final Button display_crosshair = new Button(tuningComposite, SWT.CHECK);
        display_crosshair.setText("Display Crosshair");
        display_crosshair.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	
                if (tracker == null)
                    requestTracker();
                try {
        			tracker.startTracking();
        		} catch (IOException e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
                
                if (tracker != null) {
                    tracker.displayCrosshair(
                            display_crosshair.getSelection());
                    // Create a client for the crosshair so that it will
                    // continue to run when tracking is disabled. Remove when
                    // done.
                    if (display_crosshair.getSelection()) {
                    } else {
                        if (crosshairQueue != null) {
                            crosshairQueue = null;
                        }
                    }
                    
                } else {
                    if (display_crosshair.getSelection()) {
                        displayError(DONT_CHANGE_THAT_MSG);
                        display_crosshair.setSelection(false);
                    }
                }
            }
        });

        final Composite driftComposite =
                new Composite(tuningComposite, SWT.NONE);
        driftComposite.setLayout(new GridLayout(2, false));

        final Label xDriftLabel = new Label(driftComposite, SWT.NONE);
        xDriftLabel.setText("x Drift");

        final Spinner xDrift = new Spinner(driftComposite, SWT.NONE);
        xDrift.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (tracker != null) {
                    tracker.setXDrift(xDrift.getSelection());
                } else {
                    if (xDrift.getSelection() != 0) {
                        displayError(DONT_CHANGE_THAT_MSG);
                        xDrift.setSelection(0);
                    }
                }
            }
        });
        xDrift.setMinimum(-100);
        xDrift.setMaximum(100);
        xDrift.setSelection(0);
        this.xDrift = xDrift;

        final Label yDriftLabel = new Label(driftComposite, SWT.NONE);
        yDriftLabel.setText("y Drift");

        final Spinner yDrift = new Spinner(driftComposite, SWT.NONE);
        yDrift.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (tracker != null) {
                    tracker.setYDrift(yDrift.getSelection());
                } else {
                    if (yDrift.getSelection() != 0) {
                        displayError(DONT_CHANGE_THAT_MSG);
                        yDrift.setSelection(0);
                    }
                }
            }
        });
        yDrift.setMinimum(-100);
        yDrift.setMaximum(100);
        yDrift.setSelection(0);
        this.yDrift = yDrift;

        final Composite solversComposite = new Composite(parent, SWT.NONE);
        solversComposite.setLayout(new GridLayout(2, false));
        // Configure solvers here.
        jsonSolver = new JSONGazeExportSolver();
        availableSolvers.add(jsonSolver);

        xmlSolver = new XMLGazeExportSolver();
        availableSolvers.add(xmlSolver);

        for (final ISolver solver : availableSolvers) {
            final Button solverEnabled =
                    new Button(solversComposite, SWT.CHECK);
            solverEnabled.setText(solver.friendlyName());
            solverEnabled.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                	if (sessionInfo.isConfigured()) {
                		if (solverEnabled.getSelection()) {
                			activeSolvers.addIfAbsent(solver);
                		} else {
                			while (activeSolvers.contains(solver)) {
                				activeSolvers.remove(solver);
                			}
                		}
                	} else {
                		while (activeSolvers.contains(solver)) {
                			activeSolvers.remove(solver);
                		}
                		solverEnabled.setSelection(false);
                		displayError("You must configure your Sesssion "
                				+ "Info. first.");
                	}
                }
            });
            grayedControls.addIfAbsent(solverEnabled);
            final Button solverConfig = new Button(solversComposite, SWT.PUSH);
            solverConfig.setText("...");
            solverConfig.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                	if (sessionInfo.isConfigured()) {
                		solver.displayExportFile();
                	} else {
                		displayError("You must configure your Session Info. "
                				+ "first.");
                	}
                }
            });
            grayedControls.addIfAbsent(solverConfig);
        }
        
        //Session Info Button
        final Button infoButton = new Button(buttonComposite, SWT.PUSH);
        infoButton.setText("Session Info");
        infoButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	sessionInfo.config();
            	if (sessionInfo.isConfigured()) {
            		// set all solver check buttons to checked
            		for (final Control controls : solversComposite.getChildren()) {
            			Button button = (Button) controls;
            			button.setSelection(true);
            		}
            		
            		// Configure all available solvers
            		for (final ISolver solver: availableSolvers) {
            			solver.config(sessionInfo.getSessionID(),
                				sessionInfo.getDevUsername());
            			activeSolvers.addIfAbsent(solver);
            		}
            	}
            }
        });  
        grayedControls.addIfAbsent(infoButton);
        
        //Stop Tracking Button
        final Button stopButton = new Button(buttonComposite, SWT.PUSH);
        stopButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        stopButton.setText("Stop Tracking");
        stopButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                stopTracking();
                statusLineManager.setMessage("");
                for (final Control controls : solversComposite.getChildren()) {
            		Button button = (Button) controls;
            		button.setSelection(false);
            	}
                for (final ISolver solver: activeSolvers) {
                	if(activeSolvers.contains(solver)) {
                		solver.dispose();
        				activeSolvers.remove(solver);
        			}
                }
            }
        });
        
        //Configure Filters Here
        OldJSONBasicFixationFilter oldjsonBFFilter =
        		new OldJSONBasicFixationFilter();
        OldXMLBasicFixationFilter oldxmlBFFilter =
        		new OldXMLBasicFixationFilter();
        JSONBasicFixationFilter jsonBFFilter =
        		new JSONBasicFixationFilter();
        XMLBasicFixationFilter xmlBFFilter =
        		new XMLBasicFixationFilter();
        availableFilters.add(oldjsonBFFilter);
        availableFilters.add(jsonBFFilter);
        availableFilters.add(oldxmlBFFilter);
        availableFilters.add(xmlBFFilter);
        
        final Composite filterComposite = new Composite(parent, SWT.NONE);
        filterComposite.setLayout(new GridLayout(2, false));
        
        for (final IFilter filter: availableFilters) {
        	final Button filterButton =
        			new Button(filterComposite, SWT.PUSH);
        	filterButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                	1, 1));
        	filterButton.setText(filter.getFilterName());
        	filterButton.addSelectionListener(new SelectionAdapter() {
            	@Override
            	public void widgetSelected(SelectionEvent e) {
                	File[] fileList = filter.filterUI();
                	if (fileList != null) {
                		for (int i = 0; i < fileList.length; i++) {
                			try {
                				filter.read(fileList[i]);
                				filter.process();
                				filter.export();
                			} catch(IOException exc) {
                				displayError(exc.getMessage());
                			}
                		}
                	}
            	}
        	});
        	grayedControls.add(filterButton);
        }
    }

    @Override
    public void dispose() {
        //if (gazeTransport != null)
        //    gazeTransport.quit();
        // Else there's nothing to quit.
        if (trackingInProgress) {
            stopTracking();
        }
        
        if (tracker != null)
            tracker.close();

        // Else there's nothing to close.

        getSite().getWorkbenchWindow().getPartService()
                .removePartListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
    	if(partRef.getPart(false) instanceof IEditorPart) {
    		Activator.getDefault().setActiveEditor((IEditorPart)partRef.getPart(false));
    		IEditorPart ep = (IEditorPart)partRef.getPart(true);
    		statusLineManager = ep.getEditorSite().getActionBars().getStatusLineManager();
    	}
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    	if(partRef.getPart(false) instanceof IEditorPart) {
    		Activator.getDefault().setActiveEditor((IEditorPart)partRef.getPart(false));
    		IEditorPart ep = (IEditorPart)partRef.getPart(true);
    		statusLineManager = ep.getEditorSite().getActionBars().getStatusLineManager();
    	}
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
    	if(partRef instanceof IEditorReference){
    		actionBars = getViewSite().getActionBars();
        	statusLineManager = actionBars.getStatusLineManager();
        	IEditorPart ep = (IEditorPart)partRef.getPart(true);
        	Activator.getDefault().removeHighlighter(ep);
        	Activator.getDefault().setActiveEditor(
        			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        			.getActivePage().getActiveEditor()
        	);
    	}
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        setupControls(partRef);
        HandlerBindManager.bind(partRef);
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        HandlerBindManager.unbind(partRef);
    }

    /**
     * Find styled text or browser controls within a part, set it up to be used by iTrace,
     * and extract meta-data from it.
     * 
     * @param partRef Highest-level part reference possible.
     */
    private void setupControls(IWorkbenchPartReference partRef) {
        //set up styled text manager if there is one
    	IEditorReference[] editors = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (IEditorReference editor : editors) {
            IEditorPart editorPart = editor.getEditor(true);
            if (editorPart.getAdapter(Control.class) instanceof StyledText) { //make sure editorPart contains an instance of StyledText
            	StyledText text = (StyledText) editorPart.getAdapter(Control.class); 
            	setupStyledText(editorPart, text);
            }
            //ignore anything else
        }
        //set up browser manager if there is one
        Shell workbenchShell = partRef.getPage().getWorkbenchWindow().
                getShell();
        for (Control control : workbenchShell.getChildren()) {
        	setupBrowser(control);
        }
    }

    /**
     * Find browser control, set it up to be used by iTrace,
     * and extract meta-data from it.
     * Recursive helper method for setupControls(IWorkbenchPartReference).
     * 
     * @param control control that might be a Browser
     */
    private void setupBrowser(Control control) {
        	//If composite
            if (control instanceof Composite) {
                Composite composite = (Composite) control;

                Control[] children = composite.getChildren();
                if (children.length > 0 && children[0] != null) {
                   for (Control curControl : children) 
                       setupBrowser(curControl);
                }
            }
        	
           if (control instanceof Browser) {
        	   Browser browse = (Browser) control;
        	   setupBrowser(browse);
           }
				
    }
    
    /**
     * Recursive helper method for setupControls(IWorkbenchPartReference).
     * 
     * @param editor IEditorPart which owns the StyledText in the next
     *               parameter.
     * @param control StyledText to set up.
     */
    private void setupStyledText(IEditorPart editor, StyledText control) {
        StyledText styledText = (StyledText) control;
        if (styledText.getData(KEY_AST) == null)
            styledText.setData(KEY_AST, new AstManager(editor, styledText));
    }

    /**
     * Recursive helper method for setupControls(IWorkbenchPartReference).
     * 
     * @param editor IEditorPart which owns the Browser in the next
     *               parameter.
     * @param control Browser to set up.
     */
    private void setupBrowser(Browser control) {
        Browser browser = (Browser) control;
        if (browser.getData(KEY_SO_DOM) == null)
            browser.setData(KEY_SO_DOM, new SOManager(browser));
        if (browser.getData(KEY_BR_DOM) == null)
        	browser.setData(KEY_BR_DOM, new BRManager(browser));
    }
    
    /**
     * Finds the control under the specified screen coordinates and calls its
     * gaze handler on the localized point. Returns the gaze response or null if
     * the gaze is not handled.
     */
    private IGazeResponse handleGaze(int screenX, int screenY, Gaze gaze) {

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

    private boolean requestTracker() {
        if (tracker != null) {
            // Already have a tracker. Don't need another.
            return true;
        }

        try {
            tracker = EyeTrackerFactory.getConcreteEyeTracker();
            if (tracker != null) {
                tracker.setXDrift(xDrift.getSelection());
                tracker.setYDrift(yDrift.getSelection());
                return true;
            } else {
                displayError("Either an eye tracker was not selected or an "
                        + "invalid eye tracker was selected.");
                return false;
            }
        } catch (EyeTrackerConnectException e) {
            displayError("Could not connect to eye tracker.");
            return false;
        } catch (IOException e) {
            displayError("Could not connect to eye tracker.");
            return false;
        }
    }

    private void startTracking() {
        if (trackingInProgress) {
            displayError("Tracking is already in progress.");
            return;
        }
        if (!requestTracker()) {
            // Error handling occurs in requestTracker(). Just return and
            // pretend
            // nothing happened.
            return;
        }
        eventBroker.subscribe("iTrace/newgaze", this);
        try {
			tracker.startTracking();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        if (!sessionInfo.isConfigured()) {
        	displayError("You have not configured your Session Info.");
        	return;
        }

        for (Control c : grayedControls) {
            c.setEnabled(false);
        }
        
        try {
        	sessionInfo.export();
        } catch(IOException e) {
        	displayError(e.getMessage());
        }
        Activator.getDefault().sessionStartTime = System.nanoTime();
        trackingInProgress = true;
    }

    private void stopTracking() {
        if (!trackingInProgress) {
            displayError("Tracking is not in progress.");
            return;
        }

        for (Control c : grayedControls) {
        	if(!c.isDisposed()) c.setEnabled(true);
        }

        sessionInfo.reset();
        
        if (tracker != null) {
        } else {
            // If there is no tracker, tracking should not be occurring anyways.
            displayError(FATAL_ERROR_MSG);
        }
    }
    
    private void activateHighlights(){
    	eventBroker.subscribe("iTrace/newgaze",this);
        if (tracker == null)
            requestTracker();
        try {
			tracker.startTracking();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        if (tracker != null){
        	Activator.getDefault().showTokenHighLights();
        }
    }

    private void displayError(String message) {
        MessageBox error_box = new MessageBox(rootShell, SWT.ICON_ERROR);
        error_box.setMessage(message);
        error_box.open();
    }

	@Override
	public void handleEvent(Event event) {
		if(event.getTopic() == "iTrace/newgaze"){
			String[] propertyNames = event.getPropertyNames();
			Gaze g = (Gaze)event.getProperty(propertyNames[0]);
			 if (g != null) {
	             Dimension screenRect =
	                     Toolkit.getDefaultToolkit().getScreenSize();
	             int screenX = (int) (g.getX() * screenRect.width);
	             int screenY = (int) (g.getY() * screenRect.height);
	             IGazeResponse response;
	             if(!rootShell.isDisposed()){
	            	 response = handleGaze(screenX, screenY, g);
	            	 if (response != null) {
		                 try {
		                	 if(trackingInProgress){
		                		 statusLineManager
		                 			.setMessage(String.valueOf(response.getGaze().getSessionTime()));
		                 		registerTime = System.currentTimeMillis();
		                 		eventBroker.post("iTrace/newdata", response);
		                	 }
		                 	
		                     gazeResponses.add(response);
		                     
		                     if(response instanceof IStyledTextGazeResponse){
		                     	IStyledTextGazeResponse styledTextResponse = (IStyledTextGazeResponse)response;
		                     	eventBroker.post("iTrace/newstresponse", styledTextResponse);
		                     }
		                     
		                 } catch (IllegalStateException ise) {
		                     System.err.println("Error! Gaze response queue is "
		                             + "full!");
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
