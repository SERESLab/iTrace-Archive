package edu.ysu.itrace.visualization;

import javax.swing.JPanel;

import javax.xml.parsers.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.io.File;
import java.util.Arrays;

import org.w3c.dom.*;

public class HierarchyVis extends JPanel implements MouseMotionListener, MouseListener{
	private Document dataDoc;
	private int sectionCap, responseCap, sectionIndex;
	private NodeList responseList, scesList;
	private BufferedImage backBuffer;
	private Graphics2D grphx,bbGrphx;
	private String breadCrumb;
	private AOISection currentSection;
	SCETree root;
	SCETree currentTree;
	
	private DocLine[] dlArray;
	private int[] yArray;
	private AOISection[] renderArray;
	
	private void update(SCETree sceTree){
		sectionCap = sceTree.childCount;
		renderArray = new AOISection[sectionCap];
		Arrays.fill(yArray, 40);
		for(int i=0;i<sectionCap;i++){
			renderArray[i] = new AOISection(300,50+(15*i),1000,15,sceTree.getChild(i).hasChildren(),sceTree.getChild(i).name);
		}
		
		for(int i=0;i<responseCap;i++){
			for(int j=0;j<sectionCap;j++){
				if(sceTree.getChild(j).contains(dlArray[i])){
					yArray[i] = 54+(j*15);
					break;
				}else if(dlArray[i].line > sceTree.getChild(j).endLine){
					yArray[i] = 54+(sectionCap*15);
				}
			}
		}
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		grphx = (Graphics2D)g;
		bbGrphx = (Graphics2D)backBuffer.getGraphics();
		bbGrphx.setColor(Color.white);
		bbGrphx.fillRect(0, 0, 5000, 5000);
		bbGrphx.setColor(Color.black);
		bbGrphx.setFont(new Font("Lucida Console",Font.PLAIN,20));
		bbGrphx.drawString("/"+currentTree.breadcrumb,5,34);
		bbGrphx.setFont(new Font("Lucida Console",Font.PLAIN,12));
		Color color;
		for(int i=0;i<sectionCap;i++){
			bbGrphx.setColor(Color.black);
			bbGrphx.drawString(renderArray[i].name,
					295-(bbGrphx.getFontMetrics().stringWidth(renderArray[i].name)),
					62+(i*15)
					);
			color = renderArray[i].fillColor;
			if(i%2 == 0 && (color.equals(Color.lightGray) || color.equals(Color.cyan))) color = color.darker();
			bbGrphx.setColor(color);
			bbGrphx.fill(renderArray[i]);
		}
		bbGrphx.setColor(Color.black);
		for(int i=0;i<responseCap;i++){
			bbGrphx.fillRect(300+((1000*i)/responseCap),yArray[i],2,6);
			if( i != responseCap-1)
				bbGrphx.drawLine(300+((1000*i)/responseCap)+1, 
								yArray[i]+3, 
								300+((1000*(i+1))/responseCap)+1,
								yArray[i+1]+3
								);
		}
		grphx.drawImage(backBuffer,0,0,this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		sectionIndex = 0;
		if(e.getY()<50){
			if(currentTree.getParent() != null){
				currentTree = currentTree.getParent();
				update(currentTree);
			}
		}else{
			for(int i=0;i<sectionCap;i++){
				if(renderArray[i].contains(e.getPoint()) && currentTree.getChild(i).hasChildren()){
					currentTree = currentTree.getChild(i);
					update(currentTree);
					break;
				}
			}
		}
		repaint();
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		if(!renderArray[sectionIndex].contains(e.getPoint())) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		for(int i=0;i<sectionCap;i++){
			currentSection = renderArray[i];
			if(currentSection.contains(e.getPoint())){
				sectionIndex = i;
				if(currentSection.hasChildren){
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					currentSection.fillColor = new Color(0,255,149);
				}else{
					currentSection.fillColor = new Color(230,230,230);
				}
			}else{
				if(currentSection.hasChildren){
					currentSection.fillColor = Color.cyan;
				}else{
					currentSection.fillColor = Color.lightGray;
				}
			}
		}
		repaint();
	}
	
	public HierarchyVis(File dataFile){
		super.setBackground(Color.darkGray);
		super.setOpaque(true);
		setSize(5000,5000);
		backBuffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
		root = new SCETree();
		sectionIndex = 0;
		try{
			//Parses dataFile into dataDoc.
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dataDoc = dBuilder.parse(dataFile);
			dataDoc.getDocumentElement().normalize();
			
			//Extract Data
			responseList = dataDoc.getElementsByTagName("response");
			responseCap = responseList.getLength();
			dlArray = new DocLine[responseCap];
			yArray = new int[responseCap];
			//Construct SCETree
			//fill docline array, and add file trees.
			currentTree = root;
			for(int i=0;i<responseCap;i++){
				Node response = responseList.item(i);
				NamedNodeMap attributes = response.getAttributes();
				String responseFile = attributes.getNamedItem("name").getNodeValue();
				int responseLine = Integer.parseInt(attributes.getNamedItem("line").getNodeValue());
				dlArray[i] = new DocLine(responseFile,responseLine);
				currentTree.findOrAdd(new SCETree(responseFile,root));
			}
			//Add sce trees
			for(int i=0;i<responseCap;i++){
				Node response = responseList.item(i);
				NamedNodeMap attributes = response.getAttributes();
				String responseFile = attributes.getNamedItem("name").getNodeValue();
				Node sces = response.getFirstChild();
				int index = root.ChildSearch(new SCETree(responseFile,root));
				if(sces.hasChildNodes()){
					scesList = sces.getChildNodes();
					currentTree = root.getChild(index);
					for(int j=scesList.getLength()-1;j>=0;j--){
						Node sce = scesList.item(j);
						NamedNodeMap sceAttributes = sce.getAttributes();
						String sceName = sceAttributes.getNamedItem("name").getNodeValue();
						int startLine = Integer.parseInt(sceAttributes.getNamedItem("start_line").getNodeValue());
						int endLine = Integer.parseInt(sceAttributes.getNamedItem("end_line").getNodeValue());
						int childIndex = currentTree.findOrAdd(new SCETree(sceName,responseFile,currentTree,startLine,endLine));
						currentTree = currentTree.getChild(childIndex);
					}
				}
			}
			currentTree = root;
			//add line nodes
			for(int i=0;i<responseCap;){
				boolean found = false;
				int j;
				for(j=0;j<currentTree.childCount;j++){
					if(currentTree.getChild(j).contains(dlArray[i])){
						found = true;
						break;
					}	
				}
				if(found) currentTree = currentTree.getChild(j);
				else{
					if(!(currentTree.startLine == dlArray[i].line && currentTree.endLine == dlArray[i].line))
						currentTree.findOrAdd(new SCETree("Line "+dlArray[i].line,dlArray[i].doc,currentTree,dlArray[i].line,dlArray[i].line));
					currentTree = root;
					i++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		currentTree = root;
		addMouseMotionListener(this);
		addMouseListener(this);
		update(currentTree);
	}
}
