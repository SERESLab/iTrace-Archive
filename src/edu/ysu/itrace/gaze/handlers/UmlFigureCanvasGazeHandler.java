package edu.ysu.itrace.gaze.handlers;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.onionuml.ui.graphics.figures.ClassFigure;

/**
 * Implements the gaze handler interface for a FigureCanvas widget of a UML
 * diagram editor window.
 */
public class UmlFigureCanvasGazeHandler implements IGazeHandler {
    private IEditorReference editorRef;
    private FigureCanvas canvas;

    /**
     * Constructs a new gaze handler for the target FigureCanvas object within
     * the UML diagram editor specified by partRef.
     */
    public UmlFigureCanvasGazeHandler(Object target,
                                      IWorkbenchPartReference partRef) {
        assert(target instanceof FigureCanvas);
        assert(partRef instanceof IEditorReference);
        this.canvas = (FigureCanvas) target;
        this.editorRef = (IEditorReference) partRef;
    }


    @Override
    public IGazeResponse handleGaze(final int x, final int y, final Gaze gaze) {

        IGazeResponse response = new IGazeResponse() {

            private String name = editorRef.getPartName();
            private String type = null;
            private Map<String,String> properties
                    = new Hashtable<String,String>();

            // construct the type and properties for the response
            {
                Rectangle viewport = new Rectangle(
                        canvas.getViewport().getViewLocation(),
                        canvas.getViewport().getSize());

                @SuppressWarnings("rawtypes")
                Queue<List> figures = new LinkedList<List>();
                figures.add(canvas.getContents().getChildren());

                while (!figures.isEmpty()){
                    for (Object child : figures.remove()){
                        if (child instanceof IFigure){

                            IFigure childFig = (IFigure)child;
                            Rectangle childBounds = childFig.getBounds();
                            if (viewport.intersects(childBounds)){

                                if (childBounds.contains(x + viewport.x,
                                                         y + viewport.y)) {
                                    if (childFig instanceof ClassFigure){

                                        ClassFigure classFig
                                                = (ClassFigure) childFig;
                                        int localX = x + viewport.x
                                                     - classFig.getLocation().x;
                                        int localY = y + viewport.y
                                                     - classFig.getLocation().y;

                                        this.properties.put("class_name",
                                                classFig.getNameString());

                                        int index = classFig.getPropertyIndex(
                                                localX, localY);
                                        if (index >= 0){
                                            this.properties.put("attribute",
                                                    classFig.getPropertyString(
                                                    index));
                                        }
                                        else{
                                            index = classFig.getOperationIndex(
                                                    localX, localY);
                                            if (index >= 0){
                                                this.properties.put("method",
                                                        classFig
                                                        .getOperationString(
                                                        index));
                                            }
                                        }


                                        this.type = "uml_class";
                                        break;
                                    } else {
                                        figures.add(childFig.getChildren());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public String getName() {
                return this.name;
            }

            @Override
            public String getType() {
                return this.type;
            }

            @Override
            public Map<String, String> getProperties() {
                return this.properties;
            }

            @Override
            public Gaze getGaze() {
                return gaze;
            }
        };

        return (response.getType() != null ? response : null);
    }

}
