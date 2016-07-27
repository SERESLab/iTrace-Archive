package edu.ysu.itrace;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class TokenHighlighter implements PaintListener {
	private IEditorPart editorPart;
	private StyledText styledText;
	private ProjectionViewer projectionViewer;
	private Rectangle boundingBox;
	
	private class OffsetSpan{
		int startOffset;
		int endOffset;
	}
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(boundingBox != null){
			pe.gc.drawRectangle(boundingBox);
			pe.gc.setAlpha(125);
			pe.gc.fillRectangle(boundingBox);
		}
	}

	public void redraw(){
		styledText.redraw();
	}
	
	public void update(int modelLineIndex, int column){
		int widgetLineIndex = projectionViewer.modelLine2WidgetLine(modelLineIndex);
		String lineContent = styledText.getLine(widgetLineIndex);
		//System.out.println(lineContent);
		String[] tokens = lineContent.split(" ");
		//System.out.println(tokens[0]);
		int lineOffset = styledText.getOffsetAtLine(widgetLineIndex);
		OffsetSpan tokenOffsetSpan = findTokenOffsetSpan(tokens,column,lineOffset);
		//System.out.println(styledText.getCharCount() + '\t' + tokenOffsetSpan.endOffset);
		if(tokenOffsetSpan == null) boundingBox = null;
		else boundingBox = styledText.getTextBounds(tokenOffsetSpan.startOffset,tokenOffsetSpan.endOffset);
		styledText.redraw();

	}
	
	private OffsetSpan findTokenOffsetSpan(String[] tokens, int column, int lineOffset){
		int tokenStartOffset = 0;
		int tokenIndex = 0;
		while(tokenIndex < tokens.length && tokenStartOffset+tokens[tokenIndex].length()+1 < column){
			tokenStartOffset += tokens[tokenIndex].length()+1;
			tokenIndex++;
		}
		if(tokenIndex == tokens.length) return null;
		OffsetSpan tokenOffsetSpan = new OffsetSpan();
		tokenOffsetSpan.startOffset = lineOffset+tokenStartOffset;
		tokenOffsetSpan.endOffset = lineOffset+tokenStartOffset+tokens[tokenIndex].length();
		return tokenOffsetSpan;
	}
	
	public TokenHighlighter(IEditorPart editorPart){
		this.editorPart = editorPart;
		this.styledText = (StyledText) this.editorPart.getAdapter(Control.class);
		ITextOperationTarget t = (ITextOperationTarget) editorPart.getAdapter(ITextOperationTarget.class);
		if(t instanceof ProjectionViewer) projectionViewer = (ProjectionViewer) t;
		this.styledText.addPaintListener(this);
	}
}
