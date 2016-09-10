package edu.ysu.itrace;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import edu.ysu.itrace.gaze.GazeHandlerFactory;
import edu.ysu.itrace.gaze.IGazeHandler;

/**
 * Binds on-screen elements to gaze handlers.
 */
public class HandlerBindManager {
    public static final String KEY_HANDLER = "gazeHandler";

    /**
     * Binds all controls in an IWorkbenchPartReference that is
     * an instance of IEditorPartReference to their appropriate
     * gaze handlers if the handler exists.
     * Binds the IWorkbenchPartReference that is an instance of
     * IViewPartReference to the appropriate gaze handler if the
     * handler exists.
     * @param partRef Workbench part from which to get controls.
     */
    public static void bind(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(true);
        Control control = part.getAdapter(Control.class);
        
        //is an EditorPart
        if (control != null) {
        	bindControl(partRef, control, false);
        //is a ViewPart
        } else {
        	//must be handled on a case to case basis
        	
        	//Project Explorer
        	if (part.getAdapter(ProjectExplorer.class) != null) {
        		ProjectExplorer explorer = part.getAdapter(ProjectExplorer.class);
        		//this control is the primary control associated with a ProjectExplorer
        		Control viewControl = explorer.getCommonViewer().getControl();
        		bindControl(partRef, viewControl, false);
        	}
        }
    }

    /**
     * Unbinds all controls in an IWorkbenchPartReference that is an instance
     * of IEditorPartReference which are currently bound to a gaze handler.
     * Unbinds an IWorkbenchPartReference that is an instance of IViewPartReference
     * which is currently bound to a gaze handler.
     * @param partRef Workbench part from which to get controls.
     */
    public static void unbind(IWorkbenchPartReference partRef) {
    	IWorkbenchPart part = partRef.getPart(true);
        Control control = part.getAdapter(Control.class);
        
        //is an EditorPart
        if (control != null) {
        	bindControl(partRef, control, true);
        //is a ViewPart
        } else {
        	//must be handled on a case to case basis
        	
        	//Project Explorer
        	if (part.getAdapter(ProjectExplorer.class) != null) {
        		ProjectExplorer explorer = part.getAdapter(ProjectExplorer.class);
        		//this control is the primary control associated with a ProjectExplorer
        		Control viewControl = explorer.getCommonViewer().getControl();
        		bindControl(partRef, viewControl, true);
        	}
        }
    }

    /**
     * Bind a control. If it is a composite, also bind all of its children.
     * @param partRef Same partRef parameter passed to bind()/unbind().
     * @param control Highest level control.
     * @param unbind If true, unbind instead of bind.
     */
    private static void bindControl(IWorkbenchPartReference partRef,
            Control control, boolean unbind) {
        //If composite, bind children.
        if (control instanceof Composite) {
            Composite composite = (Composite) control;

            Control[] children = composite.getChildren();
            if (children.length > 0 && children[0] != null) {
               for (Control curControl : children)
                   bindControl(partRef, curControl, unbind);
            }
        }
        
        //control should not have any data set
        //upon reaching this part of the method
        IGazeHandler handler = GazeHandlerFactory.
                               createHandler(control, partRef);
        if (handler != null && !unbind)
            control.setData(KEY_HANDLER, handler);
        else
            control.setData(KEY_HANDLER, null);
    }
}
