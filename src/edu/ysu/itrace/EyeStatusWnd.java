package edu.ysu.itrace;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class EyeStatusWnd extends JFrame {
	private class DrawingPanel extends JPanel{
		int width = 300;
		int height = 300;
		int leftRadius;
		int rightRadius;
		Point leftPos;
		Point rightPos;
		Graphics2D grphx;
		
		
		public void paintComponent( Graphics g ){
			grphx = (Graphics2D)g;
			grphx.setColor( Color.white );
			grphx.drawOval( leftPos.x-(leftRadius/2),leftPos.y-(leftRadius/2),leftRadius,leftRadius);
			grphx.drawOval( rightPos.x-(rightRadius/2),rightPos.y-(rightRadius/2),rightRadius,rightRadius);
		}
		public void update( Point leftPos,Point rightPos,double leftVal,double rightVal ){
			this.leftPos = new Point( leftPos.x/width,leftPos.y/height );
			this.rightPos = new Point ( rightPos.x/width,rightPos.y/height );
			this.leftRadius = (int)(20*leftVal);
			this.rightRadius = (int)(20*rightVal);
			repaint();
		}
		public DrawingPanel( Point leftPos,Point rightPos,double leftVal,double rightVal ){
			super.setBackground( Color.black );
			super.setOpaque( true );
			setSize( 300,200 );
			update( leftPos,rightPos,leftVal,rightVal );
		}
	}
	
	DrawingPanel drawingPanel;
	
	public void update( Point leftPos,Point rightPos,double leftVal,double rightVal ){
		drawingPanel.update( leftPos, rightPos, leftVal, rightVal );
	}
	
	public EyeStatusWnd( Point leftPos,Point rightPos,double leftVal,double rightVal ){
		super();
		drawingPanel = new DrawingPanel( leftPos,rightPos,leftVal,rightVal );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		add(drawingPanel);
		setVisible( true );
		setAlwaysOnTop( true );
	}
}
