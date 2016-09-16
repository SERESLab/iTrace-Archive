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

import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;
import edu.ysu.itrace.gaze.handlers.StyledTextGazeHandler;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class TokenHighlighter implements PaintListener {
	private class OffsetSpan{
		int startOffset;
		int endOffset;
	}
	
	
	private IEditorPart editorPart;
	private StyledText styledText;
	private ProjectionViewer projectionViewer;
	private Rectangle boundingBox;
	private LinkedBlockingQueue<Gaze> gazeQueue;
	private StyledTextGazeHandler gazeHandler;
	private Point[] points;
	private int pointIndex;
	private int numberOfPoints;
	private int nulls;
	private boolean show;
	
	
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
	
	public void updateHandleGaze(Gaze gaze){
		if(show && !styledText.isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
	               public void run() {
	            	   if(gaze != null){
	            		   nulls = 0;
							Dimension screenRect =
				                    Toolkit.getDefaultToolkit().getScreenSize();
				            int screenX = (int) (gaze.getX() * screenRect.width);
				            int screenY = (int) (gaze.getY() * screenRect.height);
				            Rectangle monitorBounds = Activator.getDefault().monitorBounds;
				            if(styledText.isDisposed()) return;
				            Rectangle editorBounds = styledText.getBounds();
				            Point screenPos = styledText.toDisplay(0, 0);
				            editorBounds.x = screenPos.x - monitorBounds.x;
				            editorBounds.y = screenPos.y - monitorBounds.y;
				            if(editorBounds.contains(screenX, screenY)){
				            	int relativeX = screenX-editorBounds.x;
				            	int relativeY = screenY-editorBounds.y;
				            	
				            	IStyledTextGazeResponse response = 
				            		gazeHandler.handleGaze(screenX, screenY, relativeX, relativeY, gaze);
				            	if(response != null && !boundingBoxContains(relativeX,relativeY)){
				            		update(response.getLine()-1,response.getCol(), relativeX, relativeY);
				            	}
				            }
			            }else{
			            	nulls++;
			            	if(nulls > 1){
			            		boundingBox = null;
								styledText.redraw();
			            	}
						}
	               }
				});
			}
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
			if(p != null && !box.contains(p)) return false;
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
	
	public TokenHighlighter(IEditorPart editorPart, boolean show){
		
		this.editorPart = editorPart;
		this.styledText = (StyledText) this.editorPart.getAdapter(Control.class);
		ITextOperationTarget t = (ITextOperationTarget) editorPart.getAdapter(ITextOperationTarget.class);
		if(t instanceof ProjectionViewer) projectionViewer = (ProjectionViewer) t;
		this.styledText.addPaintListener(this);
		this.gazeHandler = new StyledTextGazeHandler(styledText);
		this.show = show;
		this.numberOfPoints = 10;
		this.points = new Point[numberOfPoints];
		this.pointIndex = 0;
		this.nulls = 0;
		
		//this.gazeQueue = Activator.getDefault().gazeTransport.createClient();
		//System.out.println("gazeQueue");
	}
}
