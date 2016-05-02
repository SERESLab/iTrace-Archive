package edu.ysu.itrace.visualization;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.w3c.dom.*;

import edu.ysu.itrace.Activator;

public class GazeMap implements LineBackgroundListener, PaintListener, ExtendedModifyListener, Listener {
	private IEditorPart	editorPart;
	private StyledText	styledText;
	private Document 	dataDoc;
	private NodeList	responseList;
	private String		fileName;
	private File		dataFile;
	private ArrayList<Point>	gazeMapPoints = new ArrayList<Point>();
	private ProjectionViewer	viewer;


	@Override
	public void handleEvent(Event arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyText(ExtendedModifyEvent arg0) {
		styledText.redraw();
	}

	@Override
	public void paintControl(PaintEvent pe) {
		gazeMapPoints = generatePoints(responseList);
		pe.gc.setBackground(pe.gc.getForeground());
		for(int i=0;i<gazeMapPoints.size();i++){
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
	public void lineGetBackground(LineBackgroundEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	public void updateFile(){
		dataFile = 	Activator.getDefault().visFile;
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dataDoc = dBuilder.parse(dataFile);
			dataDoc.getDocumentElement().normalize();
			responseList = dataDoc.getElementsByTagName("response");
		}catch(Exception e){
			//e.printStackTrace();
		}
		styledText.redraw();
	}
	
	public ArrayList<Point> generatePoints(NodeList responses){
		ArrayList<Point> points = new ArrayList<Point>();
		for(int i=0;i<responses.getLength();i++){
			NamedNodeMap attributes = responses.item(i).getAttributes();
			String responseFileName = attributes.getNamedItem("name").getNodeValue();
			if(fileName.equals(responseFileName)){
				int line = Integer.parseInt(attributes.getNamedItem("line").getNodeValue());
				int col = Integer.parseInt(attributes.getNamedItem("col").getNodeValue());
				int wLine = viewer.modelLine2WidgetLine(line);
				//System.out.println("wLine: " + wLine);
				if(wLine != -1){
					int offset = styledText.getOffsetAtLine(wLine) + col;
					points.add(styledText.getLocationAtOffset(offset));
				}
			}
		}
		return points;
	}
	
	public GazeMap(IEditorPart editorPart){
		this.editorPart = editorPart;
		fileName = editorPart.getEditorInput().getName();
		styledText = (StyledText) editorPart.getAdapter(Control.class);
		styledText.addPaintListener(this);
		styledText.addLineBackgroundListener(this);
		styledText.addListener(SWT.MouseMove,this);
		//styledText.setTabs(4);
		ITextOperationTarget t = (ITextOperationTarget) editorPart.getAdapter(ITextOperationTarget.class);
		if(t instanceof ProjectionViewer) viewer = (ProjectionViewer) t;
		updateFile();
	}

}
