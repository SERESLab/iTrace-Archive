package edu.ysu.itrace;

import java.util.ArrayList;
import java.util.HashMap;

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

public class HeatMap implements PaintListener {
	HashMap<Integer,ArrayList<FileCoordinate>> lineFrequencies = new HashMap<Integer,ArrayList<FileCoordinate>>();
	private IEditorPart ep;
	private StyledText st;
	private ProjectionViewer projViewer;
	
	
	private boolean checkChar(char c){
		char[] delimeters = {' ', '\t','(',')','[',']','{','}','.',','};
		for(char delimeter: delimeters){
			if(c == delimeter) return true;
		}
		return false;
	}
	
	public HeatMap(IEditorPart ep){
		this.ep = ep;
		st = (StyledText)ep.getAdapter(Control.class);
		if(st == null) return;
		ITextOperationTarget t = (ITextOperationTarget) ep.getAdapter(ITextOperationTarget.class);
		if(!(t instanceof ProjectionViewer)) return;
		projViewer = (ProjectionViewer)t;
		
		st.addPaintListener(this);
	}
	
	@Override
	public void paintControl(PaintEvent pe) {
		if(!ITrace.getDefault().displayHeatMap || ITrace.getDefault().lines == null) return;
		
		
		FileCoordinate[] coordinates = ITrace.getDefault().lines;
		lineFrequencies.clear();
		for(FileCoordinate coordinate: coordinates){
			if(coordinate == null || !coordinate.filename.equals(ep.getEditorInput().getName())) continue;
			if(lineFrequencies.containsKey(coordinate.line)){
				(lineFrequencies.get(coordinate.line)).add(coordinate);
			}else{
				lineFrequencies.put(coordinate.line, new ArrayList<FileCoordinate>());
				(lineFrequencies.get(coordinate.line)).add(coordinate);
			}
		}
		
		int largestValue = 0;
		
		pe.gc.setAlpha(100);
		ArrayList<ArrayList<Point>> tokenIndices = new ArrayList<ArrayList<Point>>();
		ArrayList<int[]> magnitudes = new ArrayList<int[]>();
		
		for(int line=0; line<st.getLineCount(); line++){
			if(!lineFrequencies.containsKey(projViewer.widgetLine2ModelLine(line+1))) continue;
			String content = st.getLine(line);
			ArrayList<Point> linetokenIndices = new ArrayList<Point>();
			int start = 0;
			int end = 0;
			for(int i = 0; i<content.length();){
				while(i<content.length() && checkChar(content.charAt(i))) i++;
				if(i>=content.length()) break;
				start = i;
				while(i<content.length() && !checkChar(content.charAt(i))) i++;
				end = i-1;
				linetokenIndices.add(new Point(start,end));
			}
			int[] tokenMagnitudes = new int[linetokenIndices.size()];
			for(int i=0;i<tokenMagnitudes.length;i++) tokenMagnitudes[i] = 0;
			
			for(FileCoordinate coor: lineFrequencies.get(projViewer.widgetLine2ModelLine(line+1))){
				int index = coor.column;
				for(int i=0;i<linetokenIndices.size();i++){
					Point bounds = linetokenIndices.get(i);
					if(index >= bounds.x && index <= bounds.y){
						tokenMagnitudes[i]++;
						if(tokenMagnitudes[i] > largestValue) largestValue = tokenMagnitudes[i];
						break;
					}
				}
			}
			tokenIndices.add(linetokenIndices);
			magnitudes.add(tokenMagnitudes);
		}
			
		int index = 0;
		for(int line=0; line<st.getLineCount(); line++){	
			if(!lineFrequencies.containsKey(projViewer.widgetLine2ModelLine(line+1))) continue;
			ArrayList<Point> lineTokenIndices = tokenIndices.get(index);
			int[] tokenMagnitudes = magnitudes.get(index);
			for(int i=0;i<lineTokenIndices.size();i++){
				if(tokenMagnitudes[i] == 0) continue;
				Point bounds = lineTokenIndices.get(i);
				int startOffset = st.getOffsetAtLine(line)+bounds.x;
				Rectangle box = st.getTextBounds(startOffset, startOffset+(bounds.y-bounds.x));
				int frequency = tokenMagnitudes[i];
				if(frequency < largestValue/10) pe.gc.setBackground(new Color(pe.gc.getDevice(),0,153,204));
				else if(frequency < largestValue/5) pe.gc.setBackground(new Color(pe.gc.getDevice(),0,204,255));
				else if(frequency < (3*largestValue)/10) pe.gc.setBackground(new Color(pe.gc.getDevice(),0,255,255));
				else if(frequency < (2*largestValue)/5) pe.gc.setBackground(new Color(pe.gc.getDevice(),0,255,153));
				else if(frequency < largestValue/2) pe.gc.setBackground(new Color(pe.gc.getDevice(),204,255,51));
				else if(frequency < (3*largestValue)/5) pe.gc.setBackground(new Color(pe.gc.getDevice(),255,255,102));
				else if(frequency < (7*largestValue)/10) pe.gc.setBackground(new Color(pe.gc.getDevice(),255,204,153));
				else if(frequency < (4*largestValue)/5) pe.gc.setBackground(new Color(pe.gc.getDevice(),255,153,102));
				else if(frequency < (9*largestValue)/10) pe.gc.setBackground(new Color(pe.gc.getDevice(),255,102,0));
				else pe.gc.setBackground(new Color(pe.gc.getDevice(),255,0,0));
				
				pe.gc.fillRectangle(box);
			}
			index++;
		}
	}

}
