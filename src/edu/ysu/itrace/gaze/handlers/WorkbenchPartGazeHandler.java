package edu.ysu.itrace.gaze.handlers;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Implements a fallback handler for any UI elements lacking something
 * more specific. This handler just records the UI element's title.
 */
public class WorkbenchPartGazeHandler implements IGazeHandler {
    private String name;
    private Map<String,String> properties = new Hashtable<String,String>();

    public WorkbenchPartGazeHandler(IWorkbenchPartReference partRef) {
        this.name = partRef.getPartName();
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
                return WorkbenchPartGazeHandler.this;
            }

            @Override
            public String getType() {
                // TODO Auto-generated method stub
                return "part";
            }

            @Override
            public Map<String, String> getProperties() {
                return properties;
            }
        };
    }
}
