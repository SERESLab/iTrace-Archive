package edu.ysu.itrace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.eclipse.e4.core.services.events.IEventBroker;
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
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class GazeMap implements PaintListener, EventHandler {
	private IEditorPart ep;
	private StyledText st;
	private ProjectionViewer projectionViewer;
	private int position;
	private Timer timer;
	private IEventBroker eventBroker;
	private FileCoordinate[] coordinates;
	
	private class TimerAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			position++;
			position = position%coordinates.length;
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

				@Override
				public void run() {
					if(!st.isDisposed()) st.redraw();	
				}
			});
			eventBroker.post("GazeMapPaintEvent", null);
		}
		
	}
	private TimerAction timerAction;
	
	public GazeMap(IEditorPart ep){
		this.ep = ep;
		st = (StyledText)ep.getAdapter(Control.class);
		if(st == null) return;
		
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		projectionViewer = (ProjectionViewer)t;
		
		timerAction = new TimerAction();
		
		eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		eventBroker.subscribe("GazeMapAnimation", this);
		timer = new Timer(50,timerAction);
		st.addPaintListener(this);
		position = 0;
		
	}
	
	private boolean checkChar(char c){
		char[] delimeters = {' ', '\t','(',')','[',']','{','}','.',','};
		for(char delimeter: delimeters){
			if(c == delimeter) return true;
		}
		return false;
	}
	
	private Rectangle getBoundingBox(int lineOffset, int column, String lineContent){
		Rectangle box = null;
		
		if(column > lineContent.length() || column-1 < 0 || checkChar(lineContent.charAt(column-1))) return box;
		
		int startOffset = column-2;
		while(startOffset-1 > -1 && !checkChar(lineContent.charAt(startOffset-1))) startOffset--;
		int endOffset = column;
		while(endOffset+1 < lineContent.length() && !checkChar(lineContent.charAt(endOffset+1))) endOffset++;
		box = st.getTextBounds(lineOffset+startOffset, lineOffset+endOffset);
		return box;
	}
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(!ITrace.getDefault().displayGazeMap || ITrace.getDefault().lines == null || st.isDisposed()) return;
		
		coordinates = ITrace.getDefault().lines;
		
		Point prevPoint = null;
		int diameter = 4;
		
		pe.gc.setLineWidth(st.getLineHeight()/5);
		pe.gc.setAlpha(150);
		pe.gc.setBackground(new Color(pe.gc.getDevice(),0,0,0));	
		if(!ITrace.getDefault().animateGazeMap){
			for(int i=0;i<coordinates.length;i++){
				FileCoordinate coordinate = coordinates[i];
				if(coordinate == null || !coordinate.filename.equals(ep.getEditorInput().getName())){
					prevPoint = null;
					continue;
				}
				pe.gc.setForeground(new Color(pe.gc.getDevice(),
						(int)(255.0-(i*(256.0/(double)coordinates.length))),
						(int)((double)i*4*(256.0/(double)coordinates.length)%255),
						(int)(255.0-(i*2*(256.0/(double)coordinates.length))%255))
						);
				pe.gc.setBackground(new Color(pe.gc.getDevice(),
						(int)(255.0-(i*(256.0/(double)coordinates.length))),
						(int)((double)i*4*(256.0/(double)coordinates.length)%255),
						(int)(255.0-(i*2*(256.0/(double)coordinates.length))%255))
						);

				
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
		}else{
			prevPoint = null;
			pe.gc.setLineWidth(st.getLineHeight()/3);
			pe.gc.setAlpha(255);
			for(int i=position;i>position-5;i--){
				if(i < 0) break;
				pe.gc.setForeground(new Color(pe.gc.getDevice(),
						(int)(255.0-(i*(255.0/(double)coordinates.length))),
						(int)((double)i*4*(255.0/(double)coordinates.length)%255),
						(int)(255.0-(i*2*(255.0/(double)coordinates.length))%255))
						);
				pe.gc.setBackground(new Color(pe.gc.getDevice(),
						(int)(255.0-(i*(256.0/(double)coordinates.length))),
						(int)((double)i*4*(256.0/(double)coordinates.length)%255),
						(int)(255.0-(i*2*(256.0/(double)coordinates.length))%255))
						);
				FileCoordinate coordinate = coordinates[i];
				if(coordinate == null || !coordinate.filename.equals(ep.getEditorInput().getName())){
					prevPoint = null;
					continue;
				}
				
				int lineOffset = st.getOffsetAtLine(projectionViewer.modelLine2WidgetLine(coordinate.line-1));
				Point currentPoint = st.getLocationAtOffset(lineOffset+coordinate.column);
				
				String content = st.getLine(projectionViewer.modelLine2WidgetLine(coordinate.line-1));
				
				Rectangle box = getBoundingBox(projectionViewer.modelLine2WidgetLine(coordinate.line-1),coordinate.column,content);
				
				if(prevPoint != null){
					if(currentPoint == prevPoint){
						pe.gc.drawOval(currentPoint.x, currentPoint.y, diameter, diameter);
						diameter+= 2;
					}else{
						diameter = 0;
						//if( box != null){
							//pe.gc.setAlpha(127);
							//pe.gc.fillOval(box.x, box.y, box.width, box.width);
						//}
						pe.gc.setAlpha(255);
						pe.gc.drawLine(
								currentPoint.x,
								currentPoint.y+(st.getLineHeight()/2),
								prevPoint.x,
								prevPoint.y+(st.getLineHeight()/2));
					}
				}
				prevPoint = currentPoint;
			}
		}
	}



	@Override
	public void handleEvent(Event event) {
		if(ITrace.getDefault().animateGazeMap){
			timer.start();
		}else{
			timer.stop();
			position = 0;
		}
	}
}
