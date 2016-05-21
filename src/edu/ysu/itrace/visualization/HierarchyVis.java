package edu.ysu.itrace.visualization;

import javax.swing.JPanel;

import javax.xml.parsers.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.io.File;
import java.util.Arrays;

import org.w3c.dom.*;

public class HierarchyVis extends JPanel implements MouseMotionListener, MouseListener{
	private Document dataDoc;
	private int responseCap;
	private NodeList responseList, scesList;
	private BufferedImage backBuffer;
	private Graphics2D grphx,bbGrphx;
	SCETree root;
	SCETree currentTree;
	
	private DocLine[] dlArray;
	private int[] yArray;
	
	private int getSectionCap(SCETree tree){
		int cap = 0;
		for(int i=0;i<tree.childCount;i++){
			if(tree.getChild(i).isExpanded) 
				cap += getSectionCap(tree.getChild(i));
			else
				cap++;
		}
		return cap;
	}
	private int setSections(int place, SCETree tree){
		for(SCETree child: tree.getChildren()){
			child.section = new Rectangle(300,50+(15*place),1000,15);
			place++;
			if(child.isExpanded) place = setSections(place,child);
		}
		return place;
	}
	private int setY(int y, DocLine docLine, SCETree tree){
		for(SCETree child: tree.getChildren()){
			if(child.contains(docLine)){
				y = child.section.y+4;
				if(child.isExpanded)
					y = setY(y,docLine,child);
			}
		}
		return y;
	}
	private void findClick(Point point, SCETree tree){
		for(SCETree child: tree.getChildren()){
			if(child.hasChildren()){
				if(child.section != null && child.section.contains(point)){
					child.isExpanded = !child.isExpanded;
					return;
				}else{
					findClick(point,child);
				}
			}
		}
		return;
	}
	private void drawSections(Graphics2D grphx, SCETree tree){
		for(SCETree child: tree.getChildren()){
			grphx.setFont(new Font("Lucida Console",Font.PLAIN,12));
			if(child.isExpanded){
				grphx.setColor(Color.orange);
			}else if(child.hasChildren()){
				grphx.setColor(Color.cyan);
			}else{
				grphx.setColor(Color.lightGray);
			}
			grphx.fill(child.section);
			grphx.setColor(Color.black);
			grphx.draw(child.section);
			grphx.drawString(child.getName(),
					295-(grphx.getFontMetrics().stringWidth(child.getName())),
					child.section.y+12
					);
			if(child.isExpanded)
				drawSections(grphx, child);
		}
	}
	
	private void update(){
		Arrays.fill(yArray, 40);
		setSections(0,root);
		for(int i=0;i<yArray.length;i++){
			yArray[i] = setY(0,dlArray[i],root);
		}
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		grphx = (Graphics2D)g;
		bbGrphx = (Graphics2D)backBuffer.getGraphics();
		bbGrphx.setColor(new Color(240,240,240));
		bbGrphx.fillRect(0, 0, 5000, 5000);
		bbGrphx.setColor(Color.black);
		bbGrphx.setFont(new Font("Lucida Console",Font.PLAIN,12));
		drawSections(bbGrphx,root);
		
		for(int i=0;i<responseCap;i++){
			bbGrphx.setColor(Color.black);
			bbGrphx.fillRect(300+((1000*i)/responseCap),yArray[i],2,6);
			if( i != responseCap-1){
				bbGrphx.setColor(Color.darkGray);
				bbGrphx.drawLine(300+((1000*i)/responseCap)+1, 
								yArray[i]+3, 
								300+((1000*(i+1))/responseCap)+1,
								yArray[i+1]+3
								);
			}
		}
		grphx.drawImage(backBuffer,0,0,this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//getInsets()sectionIndex = 0;
		findClick(e.getPoint(),root);
		update();
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
		/*//if(!renderArray[sectionIndex].contains(e.getPoint())) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		for(int i=0;i<sectionCap;i++){
			currentSection = renderArray[i];
			if(currentSection.contains(e.getPoint())){
				sectionIndex = i;
				if(currentSection.hasChildren)
					setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}
		repaint();
		*/
	}
	
	public HierarchyVis(File dataFile){
		super.setBackground(Color.darkGray);
		super.setOpaque(true);
		setSize(5000,5000);
		backBuffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
		root = new SCETree();
		//sectionIndex = 0;
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
		update();
	}
}
