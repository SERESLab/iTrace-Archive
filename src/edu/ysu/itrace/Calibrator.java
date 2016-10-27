package edu.ysu.itrace;

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

public abstract class Calibrator extends JFrame {
    private class CrosshairWindow extends JWindow {
    	private class CrosshairPanel extends JPanel{
    		public CrosshairPanel(){
    			setSize(16,16);
    		}
    		@Override
    		public void paintComponent(Graphics g){
    			Graphics2D g2d = (Graphics2D)g;
    			g2d.setStroke(new BasicStroke(3));
    			super.paintComponent(g);
    			g2d.setColor(new Color(255,0,0,255));
    			g2d.drawOval(getX()+3, getY()+3, 12, 12);
    		}
    	}
    	
        private Point centre = null;

        public CrosshairWindow() {
            try {
            	super.setLocation(-10, -10);
            	setSize(16,16);
            	setBackground(new Color(0,0,255,0));
                BufferedImage crosshair =
                        Calibrator.getBufferedImage("crosshair.png");
                JPanel crosshairPanel = new CrosshairPanel();
                crosshairPanel.setOpaque(false);
                add(crosshairPanel);
                centre = new Point(8,8);
                setAlwaysOnTop(true);
            } catch (IOException e) {
                System.out.println("Failed to load crosshair icon.");
            } catch (URISyntaxException e) {
                System.out.println("Failed to load crosshair icon.");
            }
        }

        public void setLocation(int x, int y) {
            super.setLocation(x - centre.x, y - centre.y);
            
        }
    }
    
    private class CalibrationAnimation extends JPanel implements ActionListener{
    	private JFrame parent;
    	private int x,y,circleWidth,circleHeight;
    	private Point2D.Double[] calibrationPoints;
    	private Point2D.Double[] posPoints;
    	private Timer timer;
    	private double t = 0;
    	private int originIndex, destinationIndex;
    	private Dimension windowBounds;
    	
    	public void paintComponent(Graphics g){
    		super.paintComponent(g);
    		Graphics2D grphx = (Graphics2D)g;
    		grphx.setColor(Color.black);
    		for(Point2D.Double point: calibrationPoints){
    			grphx.fillOval((int)(point.x-25),
    						   (int)(point.y-25),
    						   50, 
    						   50
    							);
    							
    		}
    		grphx.setColor(Color.white);
    		grphx.fillOval(x-circleWidth/2,y-circleHeight/2,circleWidth,circleHeight);
    	}
    	
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(originIndex != destinationIndex){
				t++;
				x = (int)(calibrationPoints[originIndex].x +(calibrationPoints[destinationIndex].x-calibrationPoints[originIndex].x)*(t/60));
				y = (int)(calibrationPoints[originIndex].y +(calibrationPoints[destinationIndex].y-calibrationPoints[originIndex].y)*(t/60));
				if(t==60){
					t = 0;
					originIndex = destinationIndex;
				}
			}else{
				if(t <= 96){
					t++;
					circleWidth = 50 - ( int )( Math.sin( t*Math.PI/96 )*45 );
		            circleHeight = 50 - ( int )( Math.sin( t*Math.PI/96 )*45 );
		            if(t == 48){
		            	try {
		            		Thread.sleep(2000);
		            		useCalibrationPoint(posPoints[originIndex].x, posPoints[originIndex].y);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
				}else{
					destinationIndex = (destinationIndex+1)%10;
					if(destinationIndex == 9){
						try {
							stopCalibration();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						timer.stop();
						parent.dispose();
					}
					t = 0;
				}
			}
			repaint();
		}
		
		public void start(){
			timer.start();
		}
		
		public CalibrationAnimation(JFrame parent, Point2D.Double[] calibrationPoints){
			this.parent = parent;
			setBackground(Color.pink);
			setVisible(true);
			Random rand = new Random();
			Point2D.Double tmp;
			for(int i=8;i>0;i--){
				int j = rand.nextInt(i);
				tmp = calibrationPoints[j];
				calibrationPoints[j] = calibrationPoints[i];
				calibrationPoints[i] = tmp;
			}
			windowBounds = Toolkit.getDefaultToolkit().getScreenSize();
			posPoints = calibrationPoints;
			
			this.calibrationPoints = calibrationPoints;
			for(Point2D.Double point: this.calibrationPoints){
				point.x *= windowBounds.getWidth();
				point.y *= windowBounds.getHeight();
			}
			timer = new Timer(20, this);
			
			
			
			originIndex = 9;
			destinationIndex = 0;
			
			circleWidth = 50;
			circleHeight = 50;
			
			x = (int)(calibrationPoints[originIndex].x);
			y = (int)(calibrationPoints[originIndex].y);
		}
    	
    }
    
    private class CalibrationFrame extends JFrame{
    	private final Point2D.Double[] calibrationPoints = {
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
    	
    	public Point2D.Double[] getCalibrationPoints(){
    		return calibrationPoints;
    	}
    }

    private final int CALIBRATION_WIDTH = 3;
    private final int CALIBRATION_HEIGHT = 3;
    private final int CALIBRATION_POINTS
            = CALIBRATION_WIDTH * CALIBRATION_HEIGHT;
    private final int X_SPACING = 2;
    private final int Y_SPACING = 2;
    private JLabel[] calibrationPoints = new JLabel[CALIBRATION_POINTS];
    private final int MILISECONDS_BETWEEN_POINTS = 2000;
    private JWindow crosshairWindow = new CrosshairWindow();
    protected Point2D.Double[] calibPoints;

    public Calibrator() throws IOException {
        //Create calibration points
        JPanel grid = new JPanel(new GridLayout(
                CALIBRATION_HEIGHT + ((CALIBRATION_HEIGHT - 1) * Y_SPACING),
                CALIBRATION_WIDTH + ((CALIBRATION_WIDTH - 1) * X_SPACING)));
        BufferedImage calibrationPoint = null;
        try {
            calibrationPoint =
                Calibrator.getBufferedImage("calibration_point.png");
        } catch (IOException e) {
            throw new IOException("Could not load calibration_point.png.");
        } catch (URISyntaxException e) {
            throw new IOException("Could not load calibration_point.png.");
        }

        //Place calibration rows.
        for (int i = 1; i <= CALIBRATION_HEIGHT; ++i) {
            //Place calibration points as columns on row.
            for (int j = 1; j <= CALIBRATION_WIDTH; ++j) {
                int index = (i - 1) * CALIBRATION_WIDTH + (j - 1);
                calibrationPoints[index] = new JLabel(
                        new ImageIcon(calibrationPoint));
                calibrationPoints[index].setVisible(false);
                grid.add(calibrationPoints[index]);

                //Unless last column, place empty columns for spacing.
                if (j != CALIBRATION_WIDTH) {
                    for (int k = 0; k < X_SPACING; ++k)
                        grid.add(new JLabel(""));
                }
            }
            //Unless last row, place empty rows for spacing.
            if (i != CALIBRATION_HEIGHT) {
                for (int j = 0; j < Y_SPACING; ++j) {
                    for (int k = 0; k < CALIBRATION_WIDTH
                            + ((CALIBRATION_WIDTH - 1) * X_SPACING); ++k)
                        grid.add(new JLabel(""));
                }
            }
        }
        getContentPane().add(grid);

        setUndecorated(true);
        setAlwaysOnTop(true);
        setResizable(false);
        
        calibPoints = new java.awt.geom.Point2D.Double[9];
    }

    public void calibrate() throws CalibrationException {
    	CalibrationFrame frame = new CalibrationFrame();
    	frame.setSize(600,300);
    	calibPoints = frame.getCalibrationPoints();
    	frame.setVisible(true);
    	try {
			startCalibration();
			frame.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/*
        boolean originalCrosshairVisibility = crosshairWindow.isVisible();
        crosshairWindow.setVisible(false);

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
        device.setFullScreenWindow(this);
        setVisible(true);
        try {
            startCalibration();
            for (int i = 0; i < CALIBRATION_POINTS; ++i) {
                displayCalibrationPoint(i);
                try {
                    Thread.sleep(MILISECONDS_BETWEEN_POINTS);
                } catch (InterruptedException e) {
                    stopCalibration();
                    throw new CalibrationException(
                            "Thread.sleep interrupted.");
                }
                Dimension windowBounds = Toolkit.getDefaultToolkit()
                                          .getScreenSize();
                double x = (calibrationPoints[i].getLocationOnScreen().x
                        + (0.5 * calibrationPoints[i].getWidth()))
                        / windowBounds.width;
                double y = (calibrationPoints[i].getLocationOnScreen().y
                        + (0.5 * calibrationPoints[i].getHeight()))
                        / windowBounds.height;
                calibPoints[i] = new java.awt.geom.Point2D.Double(x,y);
                useCalibrationPoint(x, y);
            }
            stopCalibration();
        }
        //Rethrow CalibrationExceptions.
        catch (CalibrationException e) {
            throw e;
        //All JNI exceptions converted to Calibration exceptions.
        } catch (Exception e) {
            throw new CalibrationException(e.getMessage());
        }
        finally {
            setVisible(false);
        }

        crosshairWindow.setVisible(originalCrosshairVisibility);
        */
        
    }

    private void displayCalibrationPoint(int i) {
        for (int j = 0; j < CALIBRATION_POINTS; ++j)
            calibrationPoints[j].setVisible(i == j);
    }

    private static BufferedImage getBufferedImage(String resourceName)
            throws IOException, URISyntaxException {
        BufferedImage result = null;
        Bundle bundle = Platform.getBundle("edu.ysu.itrace");
        //Eclipse
        if (bundle != null) {
            URL fileUrl = bundle.getEntry("res/" + resourceName);
            URL url = FileLocator.resolve(fileUrl);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
            		url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            result = ImageIO.read(new File(uri));
        //No eclipse
        } else {
            result = ImageIO.read(new File("res/" + resourceName));
        }
        return result;
    }

    protected abstract void startCalibration() throws Exception;
    protected abstract void stopCalibration() throws Exception;
    protected abstract void useCalibrationPoint(double x, double y)
            throws Exception;
    protected abstract void displayCalibrationStatus() throws Exception;

    public void moveCrosshair(int screenX, int screenY) {
        crosshairWindow.setLocation(screenX, screenY);
    }

    public void displayCrosshair(boolean enabled) {
        crosshairWindow.setVisible(enabled);
    }
}
