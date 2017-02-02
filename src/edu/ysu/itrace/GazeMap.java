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
		Point origin = st.getLocationAtOffset(0);
		if(st == null) return;
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		Rectangle bounds = st.getBounds();
		if(!(t instanceof ProjectionViewer)) return;
		ProjectionViewer projectionViewer = (ProjectionViewer)t;
		Point prevPoint = null;
		pe.gc.setLineWidth(st.getLineHeight()/3);
		//pe.gc.setForeground(new Color(pe.gc.getDevice(),50,200,150));
		pe.gc.setAlpha(75);
		for(int i=0;i<coordinates.length;i++){
			pe.gc.setForeground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			FileCoordinate coordinate = coordinates[i];
			Point currentPoint = new Point(coordinate.x, coordinate.y);
			if(prevPoint != null) pe.gc.drawLine(
					(int)(st.getLineHeight()*((double)(currentPoint.x+origin.x)/15)),
					(int)(st.getLineHeight()*((double)(currentPoint.y+origin.y)/15)), 
					(int)(st.getLineHeight()*((double)(prevPoint.x+origin.x)/15)),
					(int)(st.getLineHeight()*((double)(prevPoint.y+origin.y)/15)));
			prevPoint = currentPoint;
		}

	}

}
