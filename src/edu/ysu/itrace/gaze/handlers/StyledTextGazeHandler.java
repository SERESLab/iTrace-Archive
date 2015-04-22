package edu.ysu.itrace.gaze.handlers;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.AstManager;
import edu.ysu.itrace.AstManager.SourceCodeEntity;
import edu.ysu.itrace.ControlView;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.gaze.IGazeHandler;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;


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
    public IStyledTextGazeResponse handleGaze(final int absoluteX, final int absoluteY,
            final int relativeX, final int relativeY, final Gaze gaze) {
        return new IStyledTextGazeResponse() {
            private AstManager astManager =
                    (AstManager) targetStyledText.getData(ControlView.KEY_AST);

            private int lineIndex = targetStyledText.getLineIndex(relativeY);
            private int lineOffset
                    = targetStyledText.getOffsetAtLine(lineIndex);
            private int offset = targetStyledText.getOffsetAtLocation(
                    new Point(relativeX, relativeY));
            private int col = offset - lineOffset;

            //(0, 0) relative to the control in absolute screen
            //coordinates.
            private Point relativeRoot = new Point(absoluteX - relativeX,
                    absoluteY - relativeY);
            //Top-left position of the first character on the line in
            //relative coordinates.
            private Point lineAnchorPosition = targetStyledText.
                    getLocationAtOffset(targetStyledText.
                    getOffsetAtLine(lineIndex));
            //To absolute.
            private Point absoluteLineAnchorPosition = new Point(
                    lineAnchorPosition.x + relativeRoot.x,
                    lineAnchorPosition.y + relativeRoot.y);

            private String name = partRef.getPartName();
            private int lineHeight = targetStyledText.getLineHeight();
            private int fontHeight = targetStyledText.getFont().getFontData()[0].getHeight();
            private AstManager.SourceCodeEntity[] entities =
                    astManager.getSCEs(lineIndex + 1, col);
            private String path = astManager.getPath();

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getGazeType() {
                return "text";
            }

            @Override
            public int getLineHeight() {
                return lineHeight;
            }

            @Override
            public int getFontHeight() {
                return fontHeight;
            }

            @Override
            public Gaze getGaze() {
                return gaze;
            }

            public IGazeHandler getGazeHandler() {
            return StyledTextGazeHandler.this;
            }

			@Override
			public int getLine() {
				return lineIndex + 1;
			}

			@Override
			public int getCol() {
				return col;
			}

			//Write out the position at the top-left of the first
            //character in absolute screen coordinates.
			@Override
			public int getLineBaseX() {
				return absoluteLineAnchorPosition.x;
			}

			@Override
			public int getLineBaseY() {
				return absoluteLineAnchorPosition.y;
			}

			@Override
			public SourceCodeEntity[] getSCEs() {
				return entities;
			}

			@Override
			public String getPath() {
				return path;
			}

        };
    }
}
