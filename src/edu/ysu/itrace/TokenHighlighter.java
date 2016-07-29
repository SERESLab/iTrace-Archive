package edu.ysu.itrace;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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

public class TokenHighlighter  extends Thread implements PaintListener {
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
	public boolean show;
	
	@Override
	public void run(){
		Display.getDefault().asyncExec(new Runnable() {
               public void run() { 
					try{
						while(show){
							Gaze gaze = gazeQueue.poll();
							if(gaze != null){
								Dimension screenRect =
				                        Toolkit.getDefaultToolkit().getScreenSize();
				                int screenX = (int) (gaze.getX() * screenRect.width);
				                int screenY = (int) (gaze.getY() * screenRect.height);
				                Rectangle monitorBounds = Activator.getDefault().monitorBounds;
				                Rectangle editorBounds = styledText.getBounds();
				                Point screenPos = styledText.toDisplay(0, 0);
				                editorBounds.x = screenPos.x - monitorBounds.x;
				                editorBounds.y = screenPos.y - monitorBounds.y;
				                if(editorBounds.contains(screenX, screenY)){
				                	int relativeX = screenX-editorBounds.x;
				                	int relativeY = screenY-editorBounds.y;
				                	IStyledTextGazeResponse response = 
				                		gazeHandler.handleGaze(screenX, screenY, relativeX, relativeY, gaze);
				                	if(response != null){
				                		update(response.getLine()-1,response.getCol());
				                	}
				                }
							}
							Thread.sleep(1000);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
               }
		});
	}
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(boundingBox != null && show){
			pe.gc.drawRectangle(boundingBox);
			pe.gc.setAlpha(125);
			pe.gc.fillRectangle(boundingBox);
		}
	}

	public void redraw(){
		styledText.redraw();
	}
	
	public void update(int lineIndex, int column){
		
        System.out.println(lineIndex + "    " + column);
        int lineOffset = styledText.getOffsetAtLine(lineIndex);
		String lineContent = styledText.getLine(lineIndex);
		System.out.println(lineContent);
		String[] tokens = lineContent.split(" ");
		OffsetSpan tokenOffsetSpan = findTokenOffsetSpan(tokens,column,lineOffset);
		if(tokenOffsetSpan == null) boundingBox = null;
		else boundingBox = styledText.getTextBounds(tokenOffsetSpan.startOffset,tokenOffsetSpan.endOffset);
		styledText.redraw();

	}
	
	public void updateHandleGaze(Gaze gaze){
		if(gaze != null){
			Display.getDefault().asyncExec(new Runnable() {
	               public void run() { 
						Dimension screenRect =
			                    Toolkit.getDefaultToolkit().getScreenSize();
			            int screenX = (int) (gaze.getX() * screenRect.width);
			            int screenY = (int) (gaze.getY() * screenRect.height);
			            Rectangle monitorBounds = Activator.getDefault().monitorBounds;
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
			            		update(response.getLine()-1,response.getCol());
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
		
	
	public void setShow(){
		show = !show;
		//if(show) this.start();
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
		this.gazeHandler = new StyledTextGazeHandler(styledText);
		this.show = false;
		//this.gazeQueue = Activator.getDefault().gazeTransport.createClient();
		//System.out.println("gazeQueue");
	}
}
