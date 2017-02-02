package edu.ysu.itrace;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		ProjectionViewer projectionViewer = (ProjectionViewer)t;
		Point prevPoint = null;
		pe.gc.setLineWidth(2);
		pe.gc.setForeground(new Color(pe.gc.getDevice(),50,200,150));
		pe.gc.setAlpha(100);
		for(int i=0;i<coordinates.length;i++){
			FileCoordinate coordinate = coordinates[i];
			Point currentPoint = st.getLocationAtOffset(st.getOffsetAtLine(coordinate.line)+coordinate.column);
			if(prevPoint != null) pe.gc.drawLine(currentPoint.x, currentPoint.y+(st.getLineHeight()/2), prevPoint.x, prevPoint.y+(st.getLineHeight()/2));
			prevPoint = currentPoint;
		}

	}

}
