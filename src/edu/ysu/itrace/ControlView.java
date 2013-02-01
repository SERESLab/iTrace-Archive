package edu.ysu.itrace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

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
	
	private IEyeTracker tracker;
	private GazeRepository gazeRepository;
	private AoiRepository aoiRepository;
	private LinkFinder linkFinder;
	private Shell rootShell;
	private UIJob listenJob = null;
	
	
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
				stopTracking();
			}
		});
		
		
		// initialize plugin
		selectTracker(0);
	}
	
	@Override
	public void dispose(){
		stopTracking();
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
			listenJob.schedule(LISTEN_MS);
		}
	}

	@Override
	public void shellClosed(ShellEvent e) {
		if(listenJob != null){
			listenJob.cancel();
		}
	}

	@Override
	public void shellDeactivated(ShellEvent e) {
		if(listenJob != null){
			listenJob.cancel();
		}
	}

	@Override
	public void shellDeiconified(ShellEvent e) {
		if(listenJob != null){
			listenJob.schedule(LISTEN_MS);
		}
	}

	@Override
	public void shellIconified(ShellEvent e) {
		if(listenJob != null){
			listenJob.cancel();
		}
	}
	
	/*
	 * Adds gaze handlers to the child controls of the specified workbench
	 * part. If remove is true, the handlers are removed instead.
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
					IGazeHandler handler = GazeHandlerFactory.createHandler(control);
					if(handler != null){
						handler.setPartReference(partRef);
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
	private void handleGaze(int screenX, int screenY){
		
		Queue<Control[]> childrenQueue = new LinkedList<Control[]>();
		Queue<Rectangle> parentBoundsQueue = new LinkedList<Rectangle>();
		childrenQueue.add(rootShell.getChildren());
		
		Rectangle rootBounds = rootShell.getBounds();
		Rectangle rootArea = rootShell.getDisplay().getClientArea();
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
					
					Object handlerObject = child.getData(KEY_HANDLER);
					if(handlerObject != null){
						IGazeHandler handler = (IGazeHandler)handlerObject;
						IGazeResponse response = handler.handleGaze(screenX - childScreenBounds.x,
								screenY - childScreenBounds.y, child);
						handleGazeResponse(response, child);
					}
				}
			}
		}
	}
	
	
	/*
	 * Handles the gaze response.
	 */
	private void handleGazeResponse(IGazeResponse response, Control control){
		
		System.out.println(response.toLogString());
		// TODO log response, generate links, etc.
	}
	
	
	private void listTrackers(){
		ArrayList<IEyeTracker> trackers = EyeTrackerFactory.getAvailableEyeTrackers();
	}
	
	private void selectTracker(int index) {
		tracker = EyeTrackerFactory.getConcreteEyeTracker(index);
	}
	
	private void startTracking(){
		if(tracker != null) {
			
			if(listenJob == null){
				listenJob = new UIJob("Tracking Gazes"){
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						
						
						
						Random rnd = new Random();
						int x = rnd.nextInt(rootShell.getBounds().width);
						int y = rnd.nextInt(rootShell.getBounds().height);
						handleGaze(x, y);
						// TODO get gaze x,y from eye tracker device, call handleGaze for each
						
						
						
						schedule(LISTEN_MS);
						return Status.OK_STATUS;
					}
				};
				listenJob.schedule(LISTEN_MS);
			}
		}
	}
	
	private void stopTracking(){
		if(tracker != null) {
			if(listenJob != null){
				listenJob.cancel();
				listenJob = null;
			}
		}
	}
}
