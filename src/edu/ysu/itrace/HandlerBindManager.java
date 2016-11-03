package edu.ysu.itrace;

import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.ysu.itrace.gaze.GazeHandlerFactory;
import edu.ysu.itrace.gaze.IGazeHandler;

/**
 * Binds on-screen elements to gaze handlers.
 */
public class HandlerBindManager {
    public static final String KEY_HANDLER = "gazeHandler";

    /**
     * Binds all controls in the Workbench shell, which can be bound to
     * their appropriate gaze handlers.
     * @param partRef Workbench part from which to get the Workbench shell.
     */
    public static void bind(IWorkbenchPartReference partRef) {
        Shell workbenchShell = partRef.getPage().getWorkbenchWindow().getShell();
        for (Control control : workbenchShell.getChildren())
        	bindControl(partRef, control, false);
    }

    /**
     * Unbinds all controls in an Workbench shell, which are currently
     * bound to a gaze handler.
     * @param partRef Workbench part from which to get the Workbench shell.
     */
    public static void unbind(IWorkbenchPartReference partRef) {
    	Shell workbenchShell = partRef.getPage().getWorkbenchWindow()
    			.getShell();
    	for (Control control : workbenchShell.getChildren())
    		bindControl(partRef, control, true);
    }

    /**
     * Bind a control. If it is a composite, also bind all of its children.
     * @param control Highest level control.
     * @param unbind If true, unbind instead of bind.
     */
    private static void bindControl(IWorkbenchPartReference partRef, Control control, boolean unbind) {
        //If composite, bind children.
        if (control instanceof Composite) {
            Composite composite = (Composite) control;

            Control[] children = composite.getChildren();
            if (children.length > 0 && children[0] != null) {
               for (Control curControl : children)
                   bindControl(partRef, curControl, unbind);
            }
        }
        
        //If key handler already set, the rest of this function is irrelevant.
        if (control.getData(KEY_HANDLER) != null)
            return;

        IGazeHandler handler = GazeHandlerFactory.
                               createHandler(control, partRef);
        if (handler != null && !unbind)
            control.setData(KEY_HANDLER, handler);
        else
            control.setData(KEY_HANDLER, null);
    }
}
