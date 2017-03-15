package edu.ysu.itrace;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;

public class GazeMap implements PaintListener {

	@Override
	public void paintControl(PaintEvent pe) {
		if(!ITrace.getDefault().displayGazeMap || ITrace.getDefault().lines == null) return;
		
		FileCoordinate[] coordinates = ITrace.getDefault().lines;
		IEditorPart ep = ITrace.getDefault().getActiveEditor();
		StyledText st = (StyledText)ep.getAdapter(Control.class);
		if(st == null) return;
		
		Point origin = st.getLocationAtOffset(0);
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		ProjectionViewer projectionViewer = (ProjectionViewer)t;
		
		Point prevPoint = null;
		
		pe.gc.setLineWidth(st.getLineHeight()/5);
		pe.gc.setAlpha(75);
		pe.gc.setBackground(new Color(pe.gc.getDevice(),0,0,0));
		
		for(int i=0;i<coordinates.length;i++){
			FileCoordinate coordinate = coordinates[i];
			pe.gc.setForeground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			
			int lineOffset = st.getOffsetAtLine(projectionViewer.modelLine2WidgetLine(coordinate.line-1));
			Point currentPoint = st.getLocationAtOffset(lineOffset+coordinate.column);
			
			pe.gc.fillOval(currentPoint.x, currentPoint.y+(st.getLineHeight()/2), 8, 8); 
			if(prevPoint != null) pe.gc.drawLine(
					(int)(st.getLineHeight()*((double)(currentPoint.x+origin.x)/15)),
					(int)(st.getLineHeight()*((double)(currentPoint.y+origin.y)/15)), 
					(int)(st.getLineHeight()*((double)(prevPoint.x+origin.x)/15)),
					(int)(st.getLineHeight()*((double)(prevPoint.y+origin.y)/15)));
			prevPoint = currentPoint;
		}
	}
}
