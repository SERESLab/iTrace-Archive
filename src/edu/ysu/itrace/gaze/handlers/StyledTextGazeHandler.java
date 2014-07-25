package edu.ysu.itrace.gaze.handlers;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.AstManager;
import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IGazeResponse;


/**
 * Implements the gaze handler interface for a StyledText widget.
 */
public class StyledTextGazeHandler implements IGazeHandler {
    private IWorkbenchPartReference partRef;
    private StyledText targetStyledText;

    /**
     * Constructs a new gaze handler for the target StyledText object within
     * the workbench part specified by partRef.
     */
    public StyledTextGazeHandler(Object target,
                                 IWorkbenchPartReference partRef) {
        assert(target instanceof StyledText);
        this.targetStyledText = (StyledText)target;
        this.partRef = partRef;
    }


    @Override
    public IGazeResponse handleGaze(final int x, final int y, final Gaze gaze) {
        final AstManager astManager =
                (AstManager) targetStyledText.getData(ControlView.KEY_AST);

        IGazeResponse response = new IGazeResponse() {

            private String name = partRef.getPartName();
            private String type = null;
            private Map<String,String> properties
                    = new Hashtable<String,String>();

            // construct the type and properties for the response
            {
                try {
                    int lineIndex = targetStyledText.getLineIndex(y);
                    int lineOffset
                            = targetStyledText.getOffsetAtLine(lineIndex);
                    int offset = targetStyledText.getOffsetAtLocation(
                            new Point(x, y));
                    int col = offset - lineOffset;

                    this.properties.put("line", String.valueOf(lineIndex + 1));
                    this.properties.put("col", String.valueOf(col));

                    AstManager.SourceCodeEntity[] entities =
                            astManager.getSCEs(lineIndex + 1, col);
                    String names = "";
                    String types = "";
                    String hows = "";
                    for (AstManager.SourceCodeEntity entity : entities) {
                        names += entity.name + ";";
                        types += entity.type.name() + ";";
                        hows += entity.how.name() + ";";
                    }
                    this.properties.put("fullyQualifiedNames",
                            names.length() > 0 ?
                            names.substring(0, names.length() - 1) : "");
                    this.properties.put("types", types.length() > 0 ?
                            types.substring(0, types.length() - 1) : "");
                    this.properties.put("hows", hows.length() > 0 ?
                            hows.substring(0, hows.length() - 1) : "");
                } catch(Exception e){}

                this.type = "text";
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
