package edu.ysu.itrace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.ysu.itrace.filters.IFilter;
import edu.ysu.itrace.filters.fixation.JSONBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.OldJSONBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.OldXMLBasicFixationFilter;
import edu.ysu.itrace.filters.fixation.XMLBasicFixationFilter;
/**
 * ViewPart for managing and controlling the plugin.
 */
public class ControlView extends ViewPart implements IPartListener2, EventHandler{
    public static final String KEY_AST = "itraceAST";
    public static final String KEY_SO_DOM = "itraceSO";
    public static final String KEY_BR_DOM = "itraceBR";
    public static final String FATAL_ERROR_MSG = "A fatal error occurred. "
            + "Restart the plugin and try again. If "
            + "the problem persists, submit a bug report.";

    private Shell rootShell;

    private CopyOnWriteArrayList<Control> grayedControls =
            new CopyOnWriteArrayList<Control>();
    
    private ArrayList<IEditorReference> setupEditors = new ArrayList<IEditorReference>();
    
    private Spinner xDrift;
    private Spinner yDrift;
    
    private CopyOnWriteArrayList<IFilter> availableFilters =
    		new CopyOnWriteArrayList<IFilter>();
    private IEventBroker eventBroker;

    @Override
    public void createPartControl(Composite parent) {
    	eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
    	eventBroker.subscribe("iTrace/error", this);
        // find root shell
        rootShell = parent.getShell();
        while (rootShell.getParent() != null) {
            rootShell = rootShell.getParent().getShell();
        }
        ITrace.getDefault().setRootShell(rootShell);
        ITrace.getDefault().monitorBounds = rootShell.getMonitor().getBounds();

        // add listener for determining part visibility
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

        final String DONT_DO_THAT_MSG =
                "You can't do that until you've "
                        + "selected a tracker in preferences.";

        // set up UI
        parent.setLayout(new RowLayout());
        
        //Button Composite start.
        final Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));

        //Calibration Button
        Button calibrateButton = new Button(buttonComposite, SWT.PUSH);
        calibrateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
        calibrateButton.setText("Calibrate");
        calibrateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	ITrace.getDefault().calibrateTracker();
            }
        });
        
        //Tracking start and stop button.
        final Button trackingButton = new Button(buttonComposite, SWT.PUSH);
        trackingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        trackingButton.setText("Start Tracking");
        trackingButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	ITrace.getDefault().setActionBars(getViewSite().getActionBars());
            	if(ITrace.getDefault().toggleTracking()){
            		if(trackingButton.getText() == "Start Tracking"){
            			trackingButton.setText("Stop Tracking");
            			for (Control c : grayedControls) {
                            c.setEnabled(false);
                        }
            		}
                	else{
                		trackingButton.setText("Start Tracking");
                		for (Control c : grayedControls) {
                            c.setEnabled(true);
                        }
                	}
            	}
            	
            }
        });
        
      //Session Info Button
        final Button infoButton = new Button(buttonComposite, SWT.PUSH);
        infoButton.setText("Session Info");
        infoButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	ITrace.getDefault().configureSessionInfo();
            }
        });  
        grayedControls.addIfAbsent(infoButton);
        
        //Eye Status Button
        final Button statusButton = new Button(buttonComposite, SWT.PUSH);
        statusButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        statusButton.setText("Eye Status");
        statusButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	ITrace.getDefault().displayEyeStatus();
            }
        });
        //Button Composite End.
        
        final String DONT_CHANGE_THAT_MSG =
                "Don't change this value until "
                        + "you've selected a tracker in preferences.";
        
        //Tuning Composite Start.
        final Composite tuningComposite = new Composite(parent, SWT.NONE);
        tuningComposite.setLayout(new RowLayout(SWT.VERTICAL));

        final Button highlight_tokens = new Button(tuningComposite, SWT.CHECK);
        highlight_tokens.setText("Highlight Tokens");
        highlight_tokens.addSelectionListener(new SelectionAdapter(){
        	@Override
            public void widgetSelected(SelectionEvent e) {
        		ITrace.getDefault().activateHighlights();
        	}
        });
        
        final Button displayCrosshair = new Button(tuningComposite, SWT.CHECK);
        displayCrosshair.setText("Display Crosshair");
        displayCrosshair.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	boolean success = ITrace.getDefault().displayCrosshair(displayCrosshair.getSelection());
            	if(success != displayCrosshair.getSelection()) displayCrosshair.setSelection(false);
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
                if (ITrace.getDefault().getTracker() != null) {
                    ITrace.getDefault().setTrackerXDrift(xDrift.getSelection());
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
            	if (ITrace.getDefault().getTracker() != null) {
            		ITrace.getDefault().setTrackerXDrift(xDrift.getSelection());
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
        //Tuning composite end.
        
        //Solvers composite begin.
        final Composite solversComposite = new Composite(parent, SWT.NONE);
        solversComposite.setLayout(new GridLayout(2, false));
        // Configure solvers here.
        
        final Button jsonSolverEnabled =
                    new Button(solversComposite, SWT.CHECK);
        jsonSolverEnabled.setText("JSON Export");
        jsonSolverEnabled.setSelection(true);
        jsonSolverEnabled.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               	if (ITrace.getDefault().sessionInfoConfigured()) {
               		if (jsonSolverEnabled.getSelection()) {
               			ITrace.getDefault().setJsonOutput(true);
               		} else {
               			ITrace.getDefault().setJsonOutput(false);
               		}
               	} else {
               		ITrace.getDefault().setJsonOutput(false);
               		jsonSolverEnabled.setSelection(false);
                	displayError("You must configure your Sesssion "
               				+ "Info. first.");
               	}
            }
        });
        grayedControls.addIfAbsent(jsonSolverEnabled);
        final Button jsonSolverConfig = new Button(solversComposite, SWT.PUSH);
        jsonSolverConfig.setText("...");
        jsonSolverConfig.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
             	if (ITrace.getDefault().sessionInfoConfigured()) {
               		ITrace.getDefault().displayJsonExportFile();
               	} else {
               		displayError("You must configure your Session Info. "
               				+ "first.");
               	}
            }
        });
        grayedControls.addIfAbsent(jsonSolverConfig);
            
       final Button xmlSolverEnabled =
    		   new Button(solversComposite, SWT.CHECK);
       xmlSolverEnabled.setText("XML Export");
       xmlSolverEnabled.setSelection(true);
       xmlSolverEnabled.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               	if (ITrace.getDefault().sessionInfoConfigured()) {
               		if (xmlSolverEnabled.getSelection()) {
               			ITrace.getDefault().setXmlOutput(true);
               		} else {
               			ITrace.getDefault().setXmlOutput(false);
               		}
               	} else {
               		ITrace.getDefault().setXmlOutput(false);
               		xmlSolverEnabled.setSelection(false);
               		displayError("You must configure your Sesssion "
              				+ "Info. first.");
             	}
           }
       });
       grayedControls.addIfAbsent(xmlSolverEnabled);
       final Button xmlSolverConfig = new Button(solversComposite, SWT.PUSH);
       xmlSolverConfig.setText("...");
       xmlSolverConfig.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               	if (ITrace.getDefault().sessionInfoConfigured()) {
               		ITrace.getDefault().displayXmlExportFile();
               	} else {
               		displayError("You must configure your Session Info. "
               				+ "first.");
             	}
            }
       });
       grayedControls.addIfAbsent(xmlSolverConfig);
       //Solver Composite end. 
        
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
        
        //Filter composite begin.
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
        //Filter composite end.
    }

    @Override
    public void dispose() {
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
    		ITrace.getDefault().setActiveEditor((IEditorPart)partRef.getPart(false));
    		IEditorPart ep = (IEditorPart)partRef.getPart(true);
    		ITrace.getDefault().setLineManager(ep.getEditorSite().getActionBars().getStatusLineManager());
    	}
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    	if(partRef.getPart(false) instanceof IEditorPart) {
    		ITrace.getDefault().setActiveEditor((IEditorPart)partRef.getPart(false));
    		IEditorPart ep = (IEditorPart)partRef.getPart(true);
    		ITrace.getDefault().setLineManager(ep.getEditorSite().getActionBars().getStatusLineManager());;
    	}
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
    	if(partRef instanceof IEditorReference){
    		setupEditors.remove(partRef);
    		ITrace.getDefault().setActionBars(getViewSite().getActionBars());
        	IEditorPart ep = (IEditorPart)partRef.getPart(true);
        	ITrace.getDefault().removeHighlighter(ep);
        	ITrace.getDefault().setActiveEditor(
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
        	if(!setupEditors.contains(editor)){
        		setupEditors.add(editor);
	            IEditorPart editorPart = editor.getEditor(true);
	            if (editorPart.getAdapter(Control.class) instanceof StyledText) { //make sure editorPart contains an instance of StyledText
	            	StyledText text = (StyledText) editorPart.getAdapter(Control.class); 
	            	setupStyledText(editorPart, text);
	            }
	            //ignore anything else
        	}
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
        if(editor.getEditorInput() instanceof FileStoreEditorInput){
        	displayError("Please import file into workspace.");
        	return;
        }
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
    
    private void displayError(String message) {
        MessageBox error_box = new MessageBox(rootShell, SWT.ICON_ERROR);
        error_box.setMessage(message);
        error_box.open();
    }

	@Override
	public void handleEvent(Event event) {
		String[] propertyNames = event.getPropertyNames();
		String message = (String)event.getProperty(propertyNames[0]);
		displayError(message);
	}

}
