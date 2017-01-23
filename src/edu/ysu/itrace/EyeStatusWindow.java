package edu.ysu.itrace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class EyeStatusWindow extends JFrame implements EventHandler {
	private class DrawingPanel extends JPanel{
		private Point leftEye;
		private Point rightEye;
		private double leftValidation;
		private double rightValidation;
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D grphx = (Graphics2D)g;
			grphx.setColor(Color.white);
			if(leftEye != null){
				grphx.fillOval(leftEye.x, leftEye.y, 20, (int)(20*leftValidation));
			}
			if(rightEye != null){
				grphx.fillOval(rightEye.x, rightEye.y, 20, (int)(20*rightValidation));
			}
		}
		
		public void update(Point leftEye, Point rightEye, double leftValidation, double rightValidation){
			this.leftEye = leftEye;
			this.rightEye = rightEye;
			this.leftValidation = leftValidation;
			this.rightValidation = rightValidation;
			repaint();
		}
		
		public DrawingPanel(){
			setBackground(Color.black);
			
		}
		
	}
	private DrawingPanel drawingPanel;
	private IEventBroker eventBroker;
	private Dimension screenSize;
	
	public EyeStatusWindow(){
		drawingPanel = new DrawingPanel();
		eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		eventBroker.subscribe("iTrace/newgaze", this);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setSize(new Dimension(screenSize.width/4,screenSize.height/4));
		setResizable(false);
		add(drawingPanel);
	}

	@Override
	public void handleEvent(Event event) {
		String[] propertyNames = event.getPropertyNames();
		Gaze gaze = (Gaze)event.getProperty(propertyNames[0]);
		Point leftEye = new Point((int)(gaze.getLeftX() * (screenSize.width/4)),(int)(gaze.getLeftY() * (screenSize.height/4)));
		Point rightEye = new Point((int)(gaze.getRightX() * (screenSize.width/4)),(int)(gaze.getRightY() * (screenSize.height/4)));
		
		drawingPanel.update(leftEye, rightEye, gaze.getLeftValidity(), gaze.getRightValidity());
	}
}
