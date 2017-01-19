package edu.ysu.itrace;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;
import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class TokenHighlighter implements PaintListener, EventHandler {
	private class OffsetSpan{
		int startOffset;
		int endOffset;
	}
	
	private StyledText styledText;
	private Rectangle boundingBox;
	private StyledTextGazeHandler gazeHandler;
	private Point[] points;
	private int pointIndex;
	private int numberOfPoints;
	private int nulls;
	private boolean show;
	private IEventBroker eventBroker;
	
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(boundingBox != null && show){
			pe.gc.setBackground(new Color(pe.gc.getDevice(),15,252,212));
			pe.gc.setForeground(new Color(pe.gc.getDevice(),0,230,172));
			pe.gc.drawRectangle(boundingBox);
			pe.gc.setAlpha(125);
			pe.gc.fillRectangle(boundingBox);
		}else if(boundingBox == null){
			boundingBox = new Rectangle(-1,-1,0,0);
			pe.gc.drawRectangle(boundingBox);
			pe.gc.setAlpha(125);
			pe.gc.fillRectangle(boundingBox);
		}
	}

	
	
	public void redraw(){
		styledText.redraw();
	}
	
	public void update(int lineIndex, int column, int x, int y){
		
        //System.out.println(lineIndex + "    " + column);
        int lineOffset = styledText.getOffsetAtLine(lineIndex);
		String lineContent = styledText.getLine(lineIndex);
		//System.out.println(lineContent);
		boundingBox = getBoundingBox(lineOffset,lineContent,x,y);
		
		//if(tokenOffsetSpan == null) boundingBox = null;
		//else boundingBox = styledText.getTextBounds(tokenOffsetSpan.startOffset,tokenOffsetSpan.endOffset);
		styledText.redraw();

	}
		
	public boolean boundingBoxContains(int x,int y){
		if(boundingBox != null) return boundingBox.contains(x,y);
		else return false;
	}
	
	public int getOffsetAtPoint(Point point){
		try{
			int offset = styledText.getOffsetAtLocation(point);
			return offset;
		}
		catch(Exception e){
			return -1;
		}
	}
		
	
	public void setShow(boolean show){
		this.show = show;
		//if(show) this.start();
	}
	
	
	private Rectangle getBoundingBox(int lineOffset, String lineContent, int x, int y){
		Rectangle box = null;
		points[pointIndex] = new Point(x,y);
		pointIndex++;
		if(pointIndex > numberOfPoints-1) pointIndex = pointIndex%numberOfPoints;
		if(containsPoints(boundingBox)) return boundingBox;
		int startOffset = 0;
		int endOffset;
		//System.out.println(startOffset + "--" + lineContent.length());
		while(startOffset < lineContent.length()){
			while(startOffset < lineContent.length() && checkChar(lineContent.charAt(startOffset))) 
				startOffset++;
			endOffset = startOffset;
			while(endOffset < lineContent.length()-1 && !checkChar(lineContent.charAt(endOffset+1))) 
				endOffset++;
			box = styledText.getTextBounds(lineOffset+startOffset, lineOffset+endOffset);
			if(containsPoints(box)) break;
			startOffset = endOffset+1;
		}
		if(box != null && !containsPoints(box)){
			box = null;
		}
		return box;
	}
	
	private boolean containsPoints(Rectangle box){
		for(Point p: points){
			if(p != null && box != null && !box.contains(p)) return false;
		}
		return true;
	}
	
	private boolean checkChar(char c){
		char[] delimeters = {' ', '\t','(',')','[',']','{','}','.',','};
		for(char delimeter: delimeters){
			if(c == delimeter) return true;
		}
		return false;
	}
	
	public TokenHighlighter(StyledText styledText, boolean show){
		this.styledText = styledText;
		this.styledText.addPaintListener(this);
		this.gazeHandler = new StyledTextGazeHandler(styledText);
		this.show = show;
		this.numberOfPoints = 10;
		this.points = new Point[numberOfPoints];
		this.pointIndex = 0;
		this.nulls = 0;
		this.eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		this.eventBroker.subscribe("iTrace/newstresponse", this);
		//this.gazeQueue = Activator.getDefault().gazeTransport.createClient();
		//System.out.println("gazeQueue");
	}



	@Override
	public void handleEvent(Event event) {
		String[] propertyNames = event.getPropertyNames();
		//System.out.println(event.getProperty(propertyNames[0]));
		IStyledTextGazeResponse response = (IStyledTextGazeResponse)event.getProperty(propertyNames[0]);
		Dimension screenRect =
                Toolkit.getDefaultToolkit().getScreenSize();
        int screenX = (int) (response.getGaze().getX() * screenRect.width);
        int screenY = (int) (response.getGaze().getY() * screenRect.height);
        Rectangle monitorBounds = ITrace.getDefault().monitorBounds;
        if(styledText.isDisposed()) return;
        Rectangle editorBounds = styledText.getBounds();
        Point screenPos = styledText.toDisplay(0, 0);
        editorBounds.x = screenPos.x - monitorBounds.x;
        editorBounds.y = screenPos.y - monitorBounds.y;
        if(editorBounds.contains(screenX, screenY)){
        	int relativeX = screenX-editorBounds.x;
        	int relativeY = screenY-editorBounds.y;
        	update(response.getLine()-1,response.getCol(), relativeX, relativeY);
        }
		
	}
}
