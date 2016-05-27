package edu.ysu.itrace.visualization;

import javax.swing.JPanel;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import java.util.ArrayList;

import edu.ysu.itrace.Activator;

public class HierarchyVis extends JPanel implements MouseMotionListener, MouseListener{
	private int responseCap;
	private BufferedImage backBuffer;
	private Graphics2D grphx,bbGrphx;
	SCETree root;
	
	private ArrayList<DocLine> DocLines;
	private int[] yArray;
	
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
				grphx.setColor(new Color(0xFFFFFF - child.file.hashCode()));
			}else if(child.hasChildren()){
				grphx.setColor(new Color(child.file.hashCode()).brighter().brighter());
			}else{
				grphx.setColor(new Color(child.file.hashCode()));
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
		setSections(0,root);
		for(int i=0;i<yArray.length;i++){
			yArray[i] = setY(0,DocLines.get(i),root);
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
				bbGrphx.drawLine(300+((1000*i)/responseCap), 
								yArray[i]+3, 
								300+((1000*(i+1))/responseCap),
								yArray[i+1]+3
								);
			}
		}
		grphx.drawImage(backBuffer,0,0,this);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
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
	
	public HierarchyVis(){
		super.setBackground(Color.darkGray);
		super.setOpaque(true);
		setSize(5000,5000);
		backBuffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
		DocLines = Activator.getDefault().extractor.getDocLines();
		responseCap = DocLines.size();
		root = Activator.getDefault().extractor.getSCETree();
		yArray = new int[responseCap];
		addMouseMotionListener(this);
		addMouseListener(this);
		update();
	}
}
