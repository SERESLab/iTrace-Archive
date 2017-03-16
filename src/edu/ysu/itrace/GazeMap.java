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
	IEditorPart ep;
	StyledText st;
	ProjectionViewer projectionViewer;
	public GazeMap(IEditorPart ep){
		this.ep = ep;
		st = (StyledText)ep.getAdapter(Control.class);
		if(st == null) return;
		
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		projectionViewer = (ProjectionViewer)t;
		
		st.addPaintListener(this);
	}
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(!ITrace.getDefault().displayGazeMap || ITrace.getDefault().lines == null) return;
		
		FileCoordinate[] coordinates = ITrace.getDefault().lines;
		
		Point prevPoint = null;
		
		pe.gc.setLineWidth(st.getLineHeight()/5);
		pe.gc.setAlpha(75);
		pe.gc.setBackground(new Color(pe.gc.getDevice(),0,0,0));		
		for(int i=0;i<coordinates.length;i++){
			FileCoordinate coordinate = coordinates[i];
			if(coordinate == null || !coordinate.filename.equals(ep.getEditorInput().getName())){
				prevPoint = null;
				continue;
			}
			pe.gc.setForeground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			
			int lineOffset = st.getOffsetAtLine(projectionViewer.modelLine2WidgetLine(coordinate.line-1));
			Point currentPoint = st.getLocationAtOffset(lineOffset+coordinate.column);
			
			//pe.gc.drawOval(currentPoint.x, currentPoint.y+(st.getLineHeight()/4), 8, 8); 
			if(prevPoint != null) pe.gc.drawLine(
					currentPoint.x,
					currentPoint.y+(st.getLineHeight()/2),
					prevPoint.x,
					prevPoint.y+(st.getLineHeight()/2));
			prevPoint = currentPoint;
		}
	}
}
