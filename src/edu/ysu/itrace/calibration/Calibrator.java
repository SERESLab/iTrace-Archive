package edu.ysu.itrace.calibration;

import edu.ysu.itrace.calibration.CrosshairWindow;
import edu.ysu.itrace.exceptions.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;

public abstract class Calibrator {
    
    private class CalibrationAnimation extends JPanel implements ActionListener{
    	private JFrame parent;
    	private int x,y,diameter;
    	private Point2D.Double[] calibrationPoints;
    	private Timer timer;
    	private double t = 0;
    	private int originIndex, destinationIndex;
    	private Dimension windowBounds;
    	
    	public void paintComponent(Graphics g){
    		super.paintComponent(g);
    		Graphics2D grphx = (Graphics2D)g;
    		grphx.setColor(Color.red);
    		grphx.fillOval(x-diameter/2,y-diameter/2,diameter,diameter);
    		grphx.setColor(Color.black);
    		grphx.fillOval(x-3, y-3, 6, 6);
    	}
    	
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try{
				if(originIndex != destinationIndex){
					t++;
					x = (int)(calibrationPoints[originIndex].x +(calibrationPoints[destinationIndex].x-calibrationPoints[originIndex].x)*(t/60));
					y = (int)(calibrationPoints[originIndex].y +(calibrationPoints[destinationIndex].y-calibrationPoints[originIndex].y)*(t/60));
					if(t==60){
						t = 0;
						originIndex = destinationIndex;
					}
				}else{
					if(t == 0){
						t++;
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							timer.stop();
							stopCalibration();
							throw new CalibrationException("Sleep Interrupted");
						}
					}else if(t <= 128){
						t++;
						diameter = 50 - ( int )( Math.sin( t*Math.PI/128 )*40 );
			            if(t == 64){
			            	useCalibrationPoint(calibrationPoints[originIndex].x/windowBounds.getWidth(), calibrationPoints[originIndex].y/windowBounds.getHeight());
			            	try {
			            		Thread.sleep(1000);
							} catch (Exception e) {
								timer.stop();
								stopCalibration();
								throw new CalibrationException("Sleep Interrupted");
							}
			            }
					}else{
						destinationIndex = (destinationIndex+1)%10;
						if(destinationIndex == 9){
							stopCalibration();
							timer.stop();
							parent.dispose();
							parent = new JFrame();
							try {
								displayCalibrationStatus(parent);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						t = 0;
					}
				}
				repaint();
			}
			catch(Exception e){
				
			}
		}
		
		public void start(){
			timer.start();
			try {
				startCalibration();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public CalibrationAnimation(CalibrationFrame parent, Point2D.Double[] calibrationPoints){
			this.parent = parent;
			setBackground(Color.lightGray);
			setVisible(true);
			Random rand = new Random();
			windowBounds = Toolkit.getDefaultToolkit().getScreenSize();
			
			this.calibrationPoints = new Point2D.Double[10];
			for(int i=0;i<10;i++){
				this.calibrationPoints[i] = 
					new Point2D.Double(
						calibrationPoints[i].x*windowBounds.getWidth(), 
						calibrationPoints[i].y*windowBounds.getHeight());
			}
			
			Point2D.Double tmp;
			for(int i=8;i>0;i--){
				int j = rand.nextInt(i);
				tmp = this.calibrationPoints[j];
				this.calibrationPoints[j] = this.calibrationPoints[i];
				this.calibrationPoints[i] = tmp;
			}

			timer = new Timer(20, this);
			
			originIndex = 9;
			destinationIndex = 0;
			
			diameter = 50;
			
			x = (int)(calibrationPoints[originIndex].x);
			y = (int)(calibrationPoints[originIndex].y);
		}
    	
    }
    
    private class CalibrationFrame extends JFrame{
    	CalibrationAnimation animation;
    	
    	public CalibrationFrame(){
    		super();
    		setTitle("calibration");
    		setUndecorated(true);
            setAlwaysOnTop(true);
            setResizable(false);
            setExtendedState(MAXIMIZED_BOTH);
            animation = new CalibrationAnimation(this,calibrationPoints);
            add(animation);
    	}
    	
    	public void start(){
    		animation.start();
    	}
    }

    private JWindow crosshairWindow = new CrosshairWindow();
    protected final Point2D.Double[] calibrationPoints = {
    		new Point2D.Double(0.1,0.1),
    		new Point2D.Double(0.1,0.5),
    		new Point2D.Double(0.1,0.9),
    		new Point2D.Double(0.5,0.1),
    		new Point2D.Double(0.5,0.5),
    		new Point2D.Double(0.5,0.9),
    		new Point2D.Double(0.9,0.1),
    		new Point2D.Double(0.9,0.5),
    		new Point2D.Double(0.9,0.9),
    		new Point2D.Double(-0.2,-0.2)
    	};

    public Calibrator() throws IOException {
    }

    public void calibrate() throws CalibrationException {
    	///*
    	CalibrationFrame frame = new CalibrationFrame();
    	frame.setSize(600,300);
    	frame.setVisible(true);
    	try {
			frame.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    protected abstract void startCalibration() throws Exception;
    protected abstract void stopCalibration() throws Exception;
    protected abstract void useCalibrationPoint(double x, double y) throws Exception;
    protected abstract void displayCalibrationStatus(JFrame frame) throws Exception;
    public void moveCrosshair(int screenX, int screenY) {
        crosshairWindow.setLocation(screenX, screenY);
    }

    public void displayCrosshair(boolean enabled) {
        crosshairWindow.setVisible(enabled);
    }
}
