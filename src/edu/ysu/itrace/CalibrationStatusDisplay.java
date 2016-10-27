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
			//System.out.println(point);
			Dimension dims = frame.getSize();
			Insets insets = frame.getInsets();
			
			int width = dims.width-(insets.left+insets.right);
			int height = dims.height-(insets.top+insets.bottom);
			
			windowDimension = new Dimension(width,height);
			if(point == null) continue;
			int x = (int)(point.x);//*windowDimension.getWidth());
			int y = (int)(point.y);//*windowDimension.getHeight());
			grphx.drawOval(x-40,y-40,80,80);
			grphx.fillOval(x-3, y-3, 6, 6);
		}
		int i = 0;
		for(Point2D.Double point: calibrationData){
			if(point == null) continue;
			if( ( point.x <= 0 || point.x >= 1 ) && ( point.y <= 0 || point.y >= 1 ) ){
				i++;
				continue;
			}
			int x = (int)(point.x*windowDimension.getWidth());
			int y = (int)(point.y*windowDimension.getHeight());
			if(i%2==0) grphx.setColor(Color.green);
			else grphx.setColor(Color.red);
			grphx.fillOval(x-2, y-2, 4, 4);
			//grphx.drawString(""+i,x-4,y-4);
			int calibx = (int)(calibrationPoints[0].x*windowDimension.getWidth());
			int caliby = (int)(calibrationPoints[0].y*windowDimension.getHeight());
			double closestdist = Math.sqrt( Math.pow((x-calibx),2)+Math.pow((y-caliby),2) );
			Point2D.Double closestPoint = calibrationPoints[0];
			for(Point2D.Double calibPoint: calibrationPoints){
				calibx = (int)(calibPoint.x*windowDimension.getWidth());
				caliby = (int)(calibPoint.y*windowDimension.getHeight());
				double dist = Math.sqrt( Math.pow((x-calibx),2)+Math.pow((y-caliby),2) );
				if(dist < closestdist){
					closestdist = dist;
					closestPoint = calibPoint;
				}
			}
			calibx = (int)(closestPoint.x*windowDimension.getWidth());
			caliby = (int)(closestPoint.y*windowDimension.getHeight());
			grphx.drawLine(x, y, calibx, caliby);
			i++;
		}
		//System.out.println(calibrationData.length);
	}
	
	public CalibrationStatusDisplay(JFrame frame, Point2D.Double[] calibrationPoints, Point2D.Double[] calibrationData){
		super.setBackground(Color.darkGray);
		super.setOpaque(true);
		
		this.windowDimension = new Dimension(600,300);
		this.calibrationPoints = calibrationPoints;
		this.calibrationData = calibrationData;
		
		this.frame = frame;
	}
	
}
