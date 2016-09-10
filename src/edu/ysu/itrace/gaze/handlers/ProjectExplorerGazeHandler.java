package edu.ysu.itrace.gaze.handlers;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Implements a Gaze Handler for the ProjectExplorer View.
 */
public class ProjectExplorerGazeHandler implements IGazeHandler {
    private String name;
    private Tree tree; //for future use
    private Map<String,String> properties = new Hashtable<String,String>();

    public ProjectExplorerGazeHandler(Object target,
    		IWorkbenchPartReference partRef) {
    	assert(target instanceof Tree);
        this.name = partRef.getPartName();
        this.tree = (Tree) target;
    }

    @Override
    public IGazeResponse handleGaze(int absoluteX, int absoluteY,
            int relativeX, int relativeY, final Gaze gaze) {
        return new IGazeResponse() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Gaze getGaze() {
                return gaze;
            }

            @Override
            public IGazeHandler getGazeHandler() {
                return ProjectExplorerGazeHandler.this;
            }

            @Override
            public String getType() {
                // TODO Auto-generated method stub
                return "view_part";
            }

            @Override
            public Map<String, String> getProperties() {
                return properties;
            }
        };
    }
}
