package edu.ysu.itrace;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class VisualizationGraphicsPanel extends ViewPart implements PaintListener, EventHandler {
	private Canvas graphicsCanvas;
	private IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
	@Override
	public void createPartControl(Composite parent) {
		eventBroker.subscribe("StyledTextPaintEvent", this);
		graphicsCanvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		graphicsCanvas.addPaintListener(this);

	}

	@Override
	public void setFocus() {
		//Do nothing.
	}

	@Override
	public void paintControl(PaintEvent pe) {
		IEditorPart ep = ITrace.getDefault().getActiveEditor();
		StyledText st = (StyledText)ep.getAdapter(Control.class);
		if(st == null) return;
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		ProjectionViewer projectionViewer = (ProjectionViewer)t;
		int lowerIndex = JFaceTextUtil.getPartialBottomIndex(st);
		int upperIndex = JFaceTextUtil.getPartialTopIndex(st);
		int range = (lowerIndex+1)-upperIndex;
		int height = st.getLineHeight();
		Point origin = st.getLocationAtOffset(st.getOffsetAtLine(upperIndex));
		pe.gc.setBackground(new Color(pe.gc.getDevice(),100,150,200));
		pe.gc.setForeground(new Color(pe.gc.getDevice(),0,0,0));
		
		for(int i=0;i<range+1;i++){
			int line = projectionViewer.widgetLine2ModelLine(upperIndex+i)+1;
			if(line == 0) continue;
			pe.gc.fillRectangle(0, i*height+origin.y, 100, height);
			pe.gc.drawRectangle(0, i*height+origin.y, 100, height);
			pe.gc.drawText(""+line, 10, i*height+origin.y);
		}
		
	}

	@Override
	public void handleEvent(Event event) {
		graphicsCanvas.redraw();	
	}

}
