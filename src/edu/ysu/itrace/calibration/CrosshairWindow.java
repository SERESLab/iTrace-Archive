package edu.ysu.itrace.calibration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.JWindow;

import org.eclipse.swt.graphics.Point;

public class CrosshairWindow extends JWindow {
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
        super.setLocation(-10, -10);
        setSize(16,16);
        setBackground(new Color(0,0,255,0));
        JPanel crosshairPanel = new CrosshairPanel();
        crosshairPanel.setOpaque(false);
        add(crosshairPanel);
        centre = new Point(8,8);
        setAlwaysOnTop(true);
    }

    public void setLocation(int x, int y) {
        super.setLocation(x - centre.x, y - centre.y);
        
    }
}