package edu.ysu.itrace;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;
import edu.ysu.itrace.gaze.GazeHandlerFactory;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * ViewPart for managing and controlling the plugin.
 */
@SuppressWarnings("restriction")
public class ControlView extends ViewPart implements IPartListener2, ShellListener {
	
	private static final int LISTEN_MS = 100;
	private static final String KEY_HANDLER = "gazeHandler";
	private static final String EOL = System.getProperty("line.separator");
	
	private IEyeTracker tracker;
	private GazeRepository gazeRepository;
	private Shell rootShell;
	private UIJob listenJob = null;
	
	private XMLStreamWriter responseWriter;
	
	
	@Override
	public void createPartControl(Composite parent) {
		
		// find root shell
		rootShell = parent.getShell();
		while (rootShell != parent.getShell()) {
			rootShell = rootShell.getShell();
		}
		rootShell.addShellListener(this);
		
		// add listener for determining part visibility
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		
		
		// set up UI
		Button calibrateButton = new Button(parent, SWT.PUSH);
		calibrateButton.setText("Calibrate");
		calibrateButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(tracker != null){
					try {
						tracker.calibrate();
					} catch (CalibrationException e1) {
						throw new RuntimeException(e1.getMessage());
					}
				}
			}
		});
		
		Button startButton = new Button(parent, SWT.PUSH);
		startButton.setText("Start Tracking");
		startButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				startTracking();
			}
		});
		
		Button stopButton = new Button(parent, SWT.PUSH);
		stopButton.setText("Stop Tracking");
		stopButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					stopTracking();
				} catch (XMLStreamException e1) {
					throw new RuntimeException(e1.getMessage());
				}
			}
		});
		
		
		// initialize plugin
		selectTracker(0);
	}
	
	@Override
	public void dispose(){
		try {
			stopTracking();
			tracker.close();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e.getMessage());
		}
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		setHandlers(partRef, false);
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		setHandlers(partRef, true);
	}
	
	@Override
	public void shellActivated(ShellEvent e) {
		if(listenJob != null){
			//listenJob.schedule(LISTEN_MS);
		}
	}

	@Override
	public void shellClosed(ShellEvent e) {
		if(listenJob != null){
			//listenJob.cancel();
		}
	}

	@Override
	public void shellDeactivated(ShellEvent e) {
		if(listenJob != null){
			//listenJob.cancel();
		}
	}

	@Override
	public void shellDeiconified(ShellEvent e) {
		if(listenJob != null){
			//listenJob.schedule(LISTEN_MS);
		}
	}

	@Override
	public void shellIconified(ShellEvent e) {
		if(listenJob != null){
			//listenJob.cancel();
		}
	}
	
	/*
	 * Adds gaze handlers to the child controls of the specified workbench
	 * part reference. If remove is true, the handlers are removed instead.
	 */
	private void setHandlers(IWorkbenchPartReference partRef, boolean remove){
		
		IWorkbenchPart part = partRef.getPart(false);
		if(part == null){
			return;
		}
		
		IWorkbenchPartSite site = part.getSite();
		if (site instanceof PartSite){
			
			Control partControl = ((PartSite)site).getPane().getControl();
			Queue<Control> controlsQueue = new LinkedList<Control>();
			controlsQueue.add(partControl);

			while(!controlsQueue.isEmpty()){
				
				Control control = controlsQueue.remove();
				boolean setHandler = false;
				
				if (control instanceof Composite) {
					Composite composite = (Composite) control;
					Control[] children = composite.getChildren();
					if (children.length > 0 && children[0] != null) {
						for (Control child : children) {
							controlsQueue.add(child);
						}
					} else {
						setHandler = true;
					}
				} else {
					setHandler = true;
				}
				
				if(setHandler && !remove){
					IGazeHandler handler = GazeHandlerFactory.createHandler(control, partRef);
					if(handler != null){
						control.setData(KEY_HANDLER, handler);
					}
				}
				else if(setHandler){
					control.setData(KEY_HANDLER, null);
				}
			}
		}
	}
	
	
	/*
	 * Finds the control under the specified screen coordinates and calls
	 * its gaze handler on the localized point.
	 */
	private void handleGaze(int screenX, int screenY, Gaze gaze){
		
		Queue<Control[]> childrenQueue = new LinkedList<Control[]>();
		Queue<Rectangle> parentBoundsQueue = new LinkedList<Rectangle>();
		childrenQueue.add(rootShell.getChildren());
		
		Rectangle rootBounds = rootShell.getBounds();
		Rectangle rootArea = rootShell.getDisplay().getPrimaryMonitor().getClientArea();
		rootBounds.x += rootArea.x;
		rootBounds.y += rootArea.y;
		parentBoundsQueue.add(rootBounds);
		
		while(!childrenQueue.isEmpty()){
			Rectangle parentBounds = parentBoundsQueue.remove();
			for(Control child : childrenQueue.remove()){
				Rectangle childScreenBounds = child.getBounds();
				childScreenBounds.x += parentBounds.x;
				childScreenBounds.y += parentBounds.y;
				if(childScreenBounds.contains(screenX, screenY)){
					if(child instanceof Composite){
						Control[] nextChildren = ((Composite)child).getChildren();
						if(nextChildren.length > 0 && nextChildren[0] != null){
							childrenQueue.add(nextChildren);
							parentBoundsQueue.add(childScreenBounds);
						}
					}
					
					IGazeHandler handler = (IGazeHandler)child.getData(KEY_HANDLER);
					if(handler != null){
						IGazeResponse response = handler.handleGaze(screenX - childScreenBounds.x,
								screenY - childScreenBounds.y);
						if(response != null){
							handleGazeResponse(response, screenX, screenY, gaze);
						}
					}
				}
			}
		}
	}
	
	
	/*
	 * Handles the gaze response.
	 */
	private void handleGazeResponse(IGazeResponse response, int screenX, int screenY, Gaze gaze){
		
		try {
			if(response.getProperties().size() > 0){
				responseWriter.writeStartElement("response");
				responseWriter.writeAttribute("file", response.getName());
				responseWriter.writeAttribute("type", response.getType());
				responseWriter.writeAttribute("x", String.valueOf(screenX));
				responseWriter.writeAttribute("y", String.valueOf(screenY));
				responseWriter.writeAttribute("timestamp", String.valueOf(gaze.getTimeStamp().getTime()));
				
				// TODO write validation codes
				
				for(Iterator<Entry<String,String>> entries = response.getProperties().entrySet().iterator();
						entries.hasNext(); ){
					Entry<String,String> pair = entries.next();
					responseWriter.writeAttribute(pair.getKey(), pair.getValue());
				}
				responseWriter.writeEndElement();
				responseWriter.writeCharacters(EOL);
			}
		} catch (XMLStreamException e) {
			// ignore write errors
		}
		
		gazeRepository.addGaze(gaze);
	}
	
	
	private void listTrackers(){
		ArrayList<IEyeTracker> trackers = EyeTrackerFactory.getAvailableEyeTrackers();
	}
	
	private void selectTracker(int index) {
		try {
			tracker = EyeTrackerFactory.getConcreteEyeTracker(index);
		} catch (EyeTrackerConnectException | CalibrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startTracking(){
		
		// create log files
		Date d = new Date();
		XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        try {
        	String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			Rectangle screenRect = rootShell.getDisplay().getPrimaryMonitor().getClientArea();
			
			responseWriter = outFactory.createXMLStreamWriter(new FileWriter(workspaceLocation + "/gaze-responses-" + d.getTime() + ".xml"));
			responseWriter.writeStartDocument("utf-8");
			responseWriter.writeCharacters(EOL);
			responseWriter.writeStartElement("environment");
			responseWriter.writeCharacters(EOL);
			responseWriter.writeStartElement("screen");
			responseWriter.writeAttribute("width", String.valueOf(screenRect.width));
			responseWriter.writeAttribute("height", String.valueOf(screenRect.height));
			responseWriter.writeEndElement();
			responseWriter.writeCharacters(EOL);
			responseWriter.writeEndElement();
			responseWriter.writeCharacters(EOL);
			responseWriter.writeStartElement("gaze-responses");
			responseWriter.writeCharacters(EOL);
        } catch (Exception e) {
			throw new RuntimeException("Log files could not be created.");
		}
		
        
		if(tracker != null) {
			
			try {
				tracker.startTracking();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(listenJob == null){
				listenJob = new UIJob("Tracking Gazes"){
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						
						Gaze g = tracker.getGaze();
						if(g != null){
							Rectangle screenRect = rootShell.getDisplay().getPrimaryMonitor().getClientArea();
							int screenX = (int) (g.getX() * screenRect.width);
							int screenY = (int) (g.getY() * screenRect.height);
							handleGaze(screenX, screenY, g);
						}
						schedule(LISTEN_MS);
						return Status.OK_STATUS;
					}
				};
				listenJob.schedule(LISTEN_MS);
			}
		}
	}
	
	private void stopTracking() throws XMLStreamException{
		
		if(tracker != null) {
			
			try {
				tracker.stopTracking();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(listenJob != null){
				listenJob.cancel();
				listenJob = null;
				
				responseWriter.writeEndElement();
				responseWriter.writeCharacters(EOL);
				responseWriter.writeEndDocument();
				responseWriter.writeCharacters(EOL);
				responseWriter.flush();
				responseWriter.close();
			}
		}
	}
}
