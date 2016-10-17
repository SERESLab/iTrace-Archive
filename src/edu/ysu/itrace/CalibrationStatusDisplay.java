package edu.ysu.itrace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.JFrame;


public class CalibrationStatusDisplay extends JPanel {
	private Point2D.Double[] calibrationPoints;
	private Point2D.Double[] calibrationData;
	private JFrame frame;
	public Dimension windowDimension;
	
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D grphx = (Graphics2D)g;
		grphx.setColor(Color.white);
		
		for(Point2D.Double point: calibrationPoints){
			System.out.println(point);
			Dimension dims = frame.getSize();
			Insets insets = frame.getInsets();
			
			int width = dims.width-(insets.left+insets.right);
			int height = dims.height-(insets.top+insets.bottom);
			
			windowDimension = new Dimension(width,height);
			
			int x = (int)(point.x*windowDimension.getWidth());
			int y = (int)(point.y*windowDimension.getHeight());
			grphx.drawOval(x-40,y-40,80,80);
			grphx.fillOval(x-3, y-3, 6, 6);
		}
		int i = 0;
		for(Point2D.Double point: calibrationData){
			int x = (int)(point.x*windowDimension.getWidth());
			int y = (int)(point.y*windowDimension.getHeight());
			if(i%2==0) grphx.setColor(Color.green);
			else grphx.setColor(Color.red);
			grphx.fillOval(x-2, y-2, 4, 4);
			grphx.drawString(""+i,x-4,y-4);
			for(int index = 0; index < 9; index++){
				if(i < Math.ceil(((index+1)*(double)calibrationData.length/9))){
					grphx.drawLine(x, y, (int)(calibrationPoints[index].x*windowDimension.getWidth()),(int)(calibrationPoints[index].y*windowDimension.getHeight()));
					break;
				}
				
			}
			i++;
		}
		System.out.println(calibrationData.length);
	}
	
	public CalibrationStatusDisplay(JFrame frame, Point2D.Double[] calibrationPoints, Point2D.Double[] calibrationData){
		super.setBackground(Color.darkGray);
		super.setOpaque(true);
		
		this.calibrationPoints = calibrationPoints;
		this.calibrationData = calibrationData;
		
		this.frame = frame;
	}
	
}
