package edu.ysu.itrace.visualization;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;

import edu.ysu.itrace.Activator;
import edu.ysu.itrace.ControlView;

public class GazeMap implements PaintListener, ExtendedModifyListener, MouseMoveListener{
	private IEditorPart					editorPart;
	private StyledText					styledText;
	private String						fileName;
	private ArrayList<Point>			gazeMapPoints = new ArrayList<Point>();
	private ProjectionViewer			viewer;
	private GazeMapPopup				pd;
	private ArrayList<VisFixation>		VisFixations;
	private HashMap<Point,VisFixation>	pointFixationHash = new HashMap<Point,VisFixation>();
	public int							cursorIndex;
	
	
	@Override
	public void modifyText(ExtendedModifyEvent arg0) {
		styledText.redraw();
	}

	@Override
	public void paintControl(PaintEvent pe) {
		if(Activator.getDefault().visFile == null) return;
		gazeMapPoints = generatePoints(VisFixations);
		pe.gc.setAlpha(255);
		pe.gc.setBackground(new Color(pe.gc.getDevice(),18,173,42));
		pe.gc.fillOval(
				gazeMapPoints.get(cursorIndex).x-8, 
				gazeMapPoints.get(cursorIndex).y-8, 
				16, 
				16
				);
		
		for(int i=0;i<gazeMapPoints.size();i++){
			int alpha = 255 - 10*Math.abs(cursorIndex-i);
			if(alpha < 0) alpha = 0;
			pe.gc.setAlpha(alpha);
			pe.gc.setBackground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			pe.gc.setForeground(new Color(pe.gc.getDevice(),255-(i%255),0+(i%255),255));
			if(i>0){
				pe.gc.drawLine(
					gazeMapPoints.get(i-1).x,
					gazeMapPoints.get(i-1).y,
					gazeMapPoints.get(i).x,
					gazeMapPoints.get(i).y
					);
			}
			pe.gc.fillOval(
					gazeMapPoints.get(i).x-3,
					gazeMapPoints.get(i).y-3, 
					6,
					6
					);
		}
	}
	@Override
	public void mouseMove(MouseEvent me) {
		Point mousePoint = new Point(me.x,me.y);
		boolean found = false;
		for(Point point: pointFixationHash.keySet()){
			if(pointDistance(point,mousePoint) < 8 && pd == null){
				//System.out.println(gazeMapPoint);
				pd = new GazeMapPopup(pointFixationHash.get(point), new Point(point.x,point.y));
				pd.open();
				found = true;
			}
			if(!found && pd != null){
				pd.close();
				pd = null;
			}
		}
		
	}
	
	public double pointDistance(Point p1,Point p2){
		return Math.sqrt(Math.pow(p1.x-3-p2.x, 2)+Math.pow(p1.y-3-p2.y, 2));
	}
	
	public void updateFile(){
		VisFixations = Activator.getDefault().extractor.getFixations();
		styledText.redraw();
	}
	
	public ArrayList<Point> generatePoints(ArrayList<VisFixation> vfs){
		ArrayList<Point> points = new ArrayList<Point>();
		for(VisFixation vf: vfs){
			if(vf.file.equals(fileName)){
				int line = viewer.modelLine2WidgetLine(vf.line);
				if(line == -1){
					System.out.println(vf.line + "\t" + vf.column);
					continue;
				}
				int offset = styledText.getOffsetAtLine(line) + vf.column;
				//offset = viewer.modelOffset2WidgetOffset(offset);
				//if(offset == -1){
				//	System.out.println(vf.line + "\t" + vf.column);
				//	continue;
				//}
				Point point = styledText.getLocationAtOffset(offset);
				points.add(point);
				pointFixationHash.put(point, vf);
			}
		}
		/*
				int offset = styledText.getOffsetAtLine(line) + col;
				points.add(styledText.getLocationAtOffset(offset));
				//int wLine = viewer.modelLine2WidgetLine(line);
				//System.out.println("wLine: " + wLine);
				/*if(wLine != -1){
					int offset = styledText.getOffsetAtLine(wLine) + col;
					points.add(styledText.getLocationAtOffset(offset));
		
		*/
		return points;
	}
	
	public GazeMap(IEditorPart editorPart){
		this.editorPart = editorPart;
		fileName = this.editorPart.getEditorInput().getName();
		styledText = (StyledText) editorPart.getAdapter(Control.class);
		styledText.addPaintListener(this);
		styledText.addMouseMoveListener(this);
		ITextOperationTarget t = (ITextOperationTarget) editorPart.getAdapter(ITextOperationTarget.class);
		if(t instanceof ProjectionViewer) viewer = (ProjectionViewer) t;
		updateFile();
	}
	
	public void redraw(){
		styledText.redraw();
	}

	

	

}
