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
import org.eclipse.swt.graphics.Rectangle;
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
		Rectangle size = graphicsCanvas.getBounds();
		FileCoordinate[] lines = ITrace.getDefault().lines;
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
		pe.gc.setForeground(new Color(pe.gc.getDevice(),0,0,0));
		
		int caretLine = projectionViewer.widgetLine2ModelLine(st.getLineAtOffset(st.getCaretOffset()))+1;
		pe.gc.setFont(st.getFont());
		int space = (pe.gc.textExtent(""+st.getLineCount())).x;
		for(int i=0;i<range+1;i++){
			pe.gc.setBackground(new Color(pe.gc.getDevice(),100,100,100));
			int line = projectionViewer.widgetLine2ModelLine(upperIndex+i)+1;
			if(line == 0) continue;
			if(line == caretLine) pe.gc.setBackground(new Color(pe.gc.getDevice(),150,200,250));
			pe.gc.fillRectangle(0, i*height+origin.y, size.width-space-2, height);
			pe.gc.drawRectangle(0, i*height+origin.y, size.width-space-2, height);
			pe.gc.setBackground(new Color(pe.gc.getDevice(),255,255,255));
			pe.gc.drawText(""+line, size.width-space, i*height+origin.y);
			//pe.gc.drawText(""+line, 10, i*height+origin.y);
		}
		pe.gc.setBackground(new Color(pe.gc.getDevice(),0,0,0));
		pe.gc.setLineWidth(height/3);
		if(lines == null) return;
		Point prevPoint = null;
		for(int i=0; i<lines.length;i++){
			if(lines[i] == null || !lines[i].filename.equals(ep.getEditorInput().getName())){
				prevPoint = null;
				continue;
			}
			pe.gc.setForeground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			int line = lines[i].line;
			Point startingPoint = new Point(0,0);
			//System.out.println(projectionViewer.widgetLine2ModelLine(upperIndex)+1);
			if(line < projectionViewer.widgetLine2ModelLine(upperIndex)+1){
				//System.out.println(line);
				startingPoint.y = -5;
			}
			else if(line > projectionViewer.widgetLine2ModelLine(lowerIndex)+1){
				startingPoint.y = size.height+5;
			}
			else if(line != projectionViewer.modelLine2WidgetLine(line)){
				if(projectionViewer.modelLine2WidgetLine(line) == -1){
					
					while(projectionViewer.modelLine2WidgetLine(line) == -1 && line != -1) line--;
					line++;
					startingPoint.y = projectionViewer.modelLine2WidgetLine(line - upperIndex-1)*height+origin.y+(height/2);
					
					if(startingPoint.y < 0) startingPoint.y = -startingPoint.y;
				}else{
					startingPoint.y = (projectionViewer.modelLine2WidgetLine(line)-upperIndex-1)*height+origin.y+(height/2);
				}
				
			}else{
				startingPoint.y = projectionViewer.modelLine2WidgetLine(line - upperIndex-1)*height+origin.y+(height/2);
				//System.out.println(startingPoint.y);
			}
			startingPoint.x = (int)((double)((size.width-space)*i)/lines.length);
			
			if(prevPoint != null)
				pe.gc.drawLine(startingPoint.x, startingPoint.y, prevPoint.x, prevPoint.y);
			
			//if(startingPoint.y < 0 && startingPoint.y != -5)
				//System.out.println(""+line+ '\t'+ startingPoint);
			prevPoint = startingPoint;
		}
		
		
		
	}

	@Override
	public void handleEvent(Event event) {
		graphicsCanvas.redraw();	
	}

}
