package edu.ysu.itrace;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.solvers.ISolver;
import edu.ysu.itrace.solvers.JSONGazeExportSolver;
import edu.ysu.itrace.solvers.XMLGazeExportSolver;
import fj.data.Option;
import static fj.data.Option.some;
import static fj.data.Option.none;

/**
 * ViewPart for managing and controlling the plugin.
 */
public class ControlView extends ViewPart implements IPartListener2,
        ShellListener {
    private static final int POLL_GAZES_MS = 5;
    public static final String KEY_AST = "itraceAST";
    public static final String FATAL_ERROR_MSG = "A fatal error occurred. "
            + "Restart the plugin and try again. If "
            + "the problem persists, submit a bug report.";

    private Option<IEyeTracker> tracker = none();
    private Shell rootShell;

    private CopyOnWriteArrayList<Control> grayedControls =
            new CopyOnWriteArrayList<Control>();

    private Option<GazeTransport> gazeTransport = none();
    private Option<LinkedBlockingQueue<Gaze>> standardTrackingQueue = none();
    private Option<LinkedBlockingQueue<Gaze>> crosshairQueue = none();

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

    /*
     * Gets gazes from the eye tracker, calls gaze handlers, and adds responses
     * to the queue for the response handler thread to process.
     */
    private UIJob gazeHandlerJob = new UIJob("Tracking Gazes") {
        {
            setPriority(INTERACTIVE);
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (standardTrackingQueue.isNone())
                return Status.OK_STATUS;

            int loops = 0;
            Gaze g = null;
            do {
                // Method returns above if standardTrackingQueue is none.
                g = standardTrackingQueue.some().poll();
                if (g != null) {
                    Dimension screenRect =
                            Toolkit.getDefaultToolkit().getScreenSize();
                    int screenX = (int) (g.getX() * screenRect.width);
                    int screenY = (int) (g.getY() * screenRect.height);
                    IGazeResponse response = handleGaze(screenX, screenY, g);

                    if (response != null) {
                        try {
                            gazeResponses.add(response);
                        } catch (IllegalStateException ise) {
                            System.err.println("Error! Gaze response queue is "
                                    + "full!");
                        }
                    }
                }

                if (trackingInProgress || g != null) {
                    schedule(POLL_GAZES_MS);
                } else {
                    gazeHandlerJob.cancel();
                }
                ++loops;
            } while (loops < 15 && g != null);

            return Status.OK_STATUS;
        }
    };

    private class ResponseHandlerThread extends Thread {
        @Override
        public void run() {
            for (ISolver solver : activeSolvers) {
                solver.init();
            }

            while (true) {
                if (!trackingInProgress && gazeResponses.size() <= 0) {
                    break;
                }

                IGazeResponse response = gazeResponses.poll();

                if (response != null) {
                    for (ISolver solver : activeSolvers) {
                        solver.process(response);
                    }
                }
            }
            for (ISolver solver : activeSolvers) {
                solver.dispose();
            }
        }
    }

    private ResponseHandlerThread responseHandlerThread = null;

    @Override
    public void createPartControl(Composite parent) {
        // find root shell
        rootShell = parent.getShell();
        while (rootShell.getParent() != null) {
            rootShell = rootShell.getParent().getShell();
        }
        rootShell.addShellListener(this);

        // add listener for determining part visibility
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);

        final String DONT_DO_THAT_MSG =
                "You can't do that until you've "
                        + "selected a tracker in preferences.";

        // set up UI
        parent.setLayout(new RowLayout());

        final Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));

        final Button startButton = new Button(buttonComposite, SWT.PUSH);
        startButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        startButton.setText("Start Tracking");
        startButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                startTracking();
            }
        });

        final Button stopButton = new Button(buttonComposite, SWT.PUSH);
        stopButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));
        stopButton.setText("Stop Tracking");
        stopButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                stopTracking();
            }
        });

        Button calibrateButton = new Button(buttonComposite, SWT.PUSH);
        calibrateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
        calibrateButton.setText("Calibrate");
        calibrateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                requestTracker();
                if (tracker.isSome()) {
                    try {
                        tracker.some().calibrate();
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

        final Button displayStatus = new Button(buttonComposite, SWT.PUSH);
        displayStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 1, 1));
        displayStatus.setText("Display Status");
        displayStatus.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (gazeTransport.isSome()) {
                    (new EyeStatusView(rootShell, gazeTransport.some())).open();
                } else {
                    displayError("You must initialise a tracker first.");
                }
            }
        });

        final String DONT_CHANGE_THAT_MSG =
                "Don't change this value until "
                        + "you've selected a tracker in preferences.";

        final Composite tuningComposite = new Composite(parent, SWT.NONE);
        tuningComposite.setLayout(new RowLayout(SWT.VERTICAL));

        final Button display_crosshair = new Button(tuningComposite, SWT.CHECK);
        display_crosshair.setText("Display Crosshair");
        display_crosshair.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (tracker.isNone())
                    requestTracker();

                if (tracker.isSome()) {
                    tracker.some().displayCrosshair(
                            display_crosshair.getSelection());
                    // Create a client for the crosshair so that it will
                    // continue to run when tracking is disabled. Remove when
                    // done.
                    if (display_crosshair.getSelection()) {
                        if (gazeTransport.isSome())
                            crosshairQueue =
                                    gazeTransport.some().createClient();
                    } else {
                        if (crosshairQueue.isSome()) {
                            gazeTransport.some().removeClient(
                                    crosshairQueue.some());
                            crosshairQueue = none();
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
                if (tracker.isSome()) {
                    tracker.some().setXDrift(xDrift.getSelection());
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
                if (tracker.isSome()) {
                    tracker.some().setYDrift(yDrift.getSelection());
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
        jsonSolver = new JSONGazeExportSolver(rootShell);
        availableSolvers.add(jsonSolver);

        xmlSolver = new XMLGazeExportSolver(rootShell);
        availableSolvers.add(new XMLGazeExportSolver(rootShell));

        for (final ISolver solver : availableSolvers) {
            final Button solverEnabled =
                    new Button(solversComposite, SWT.CHECK);
            solverEnabled.setText(solver.friendlyName());
            solverEnabled.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (solverEnabled.getSelection()) {
                        activeSolvers.addIfAbsent(solver);
                    } else {
                        while (activeSolvers.contains(solver)) {
                            activeSolvers.remove(solver);
                        }
                    }
                }
            });
            grayedControls.addIfAbsent(solverEnabled);
            final Button solverConfig = new Button(solversComposite, SWT.PUSH);
            solverConfig.setText("...");
            solverConfig.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    solver.config();
                }
            });
            grayedControls.addIfAbsent(solverConfig);
        }

    }

    @Override
    public void dispose() {
        if (gazeTransport.isSome())
            gazeTransport.some().quit();
        // Else there's nothing to quit.

        if (trackingInProgress) {
            stopTracking();
        }
        if (tracker != null)
            tracker.some().close();
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
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
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
        setupStyledText(partRef);
        HandlerBindManager.bind(partRef);
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        HandlerBindManager.unbind(partRef);
    }

    @Override
    public void shellActivated(ShellEvent e) {
        gazeHandlerJob.schedule(POLL_GAZES_MS);
    }

    @Override
    public void shellClosed(ShellEvent e) {
        gazeHandlerJob.cancel();
    }

    @Override
    public void shellDeactivated(ShellEvent e) {
        gazeHandlerJob.cancel();
    }

    @Override
    public void shellDeiconified(ShellEvent e) {
        gazeHandlerJob.schedule(POLL_GAZES_MS);
    }

    @Override
    public void shellIconified(ShellEvent e) {
        gazeHandlerJob.cancel();
    }

    /**
     * Find styled text controls within a part, set it up to be used by iTrace,
     * and extract meta-data from it.
     * 
     * @param partRef Highest-level part reference possible.
     */
    private void setupStyledText(IWorkbenchPartReference partRef) {
        IEditorReference[] editors = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (IEditorReference editor : editors) {
            IEditorPart editorPart = editor.getEditor(true);
            StyledText text = (StyledText) editorPart.getAdapter(Control.class);
            setupStyledText(editorPart, text);
        }
    }

    /**
     * Recursive helper method for setupStyledText(IWorkbenchPartReference).
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
        if (tracker.isSome()) {
            // Already have a tracker. Don't need another.
            return true;
        }

        try {
            tracker = EyeTrackerFactory.getConcreteEyeTracker();
            if (tracker.isSome()) {
                tracker.some().setXDrift(xDrift.getSelection());
                tracker.some().setYDrift(yDrift.getSelection());

                gazeTransport = some(new GazeTransport(tracker.some()));
                gazeTransport.some().start();
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

        for (Control c : grayedControls) {
            c.setEnabled(false);
        }

        if (gazeTransport.isSome()) {
            standardTrackingQueue = gazeTransport.some().createClient();

            if (standardTrackingQueue.isSome() && responseHandlerThread == null) {
                responseHandlerThread = new ResponseHandlerThread();
                responseHandlerThread.start();
                gazeHandlerJob.schedule(POLL_GAZES_MS);
                trackingInProgress = true;
            } else {
                displayError(FATAL_ERROR_MSG);
            }
        } else {
            // If tracking is in progress, the gaze transport should be some.
            displayError(FATAL_ERROR_MSG);
        }
    }

    private void stopTracking() {
        if (!trackingInProgress) {
            displayError("Tracking is not in progress.");
            return;
        }

        for (Control c : grayedControls) {
            c.setEnabled(true);
        }

        if (tracker.isSome()) {
            if (gazeTransport.isSome()) {
                if (gazeTransport.some().removeClient(
                        standardTrackingQueue.some())) {
                    trackingInProgress = false;
                    standardTrackingQueue = none();
                    responseHandlerThread = null;
                }
            } else {
                // If gaze transport is null, it shouldn't be tracking.
                displayError(FATAL_ERROR_MSG);
            }
        } else {
            // If there is no tracker, tracking should not be occurring anyways.
            displayError(FATAL_ERROR_MSG);
        }
    }

    private void displayError(String message) {
        MessageBox error_box = new MessageBox(rootShell, SWT.ICON_ERROR);
        error_box.setMessage(message);
        error_box.open();
    }
}
