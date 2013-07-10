package edu.ysu.itrace;

import edu.ysu.itrace.exceptions.*;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;

public abstract class Calibrator extends JFrame {
    private class CrosshairWindow extends JWindow {
        private Point centre = null;

        public CrosshairWindow() {
            try {
                BufferedImage crosshair =
                        Calibrator.getBufferedImage("crosshair.png");
                add(new JLabel(new ImageIcon(crosshair)));
                centre = new Point((int) (crosshair.getWidth() / 2),
                        (int) (crosshair.getHeight() / 2));
                pack();
                setAlwaysOnTop(true);
            } catch (IOException | URISyntaxException e) {
                System.out.println("Failed to load crosshair icon.");
            }
        }

        public void setLocation(int x, int y) {
            super.setLocation(x - centre.x, y - centre.y);
        }
    }

    private final int CALIBRATION_POINTS = 9;
    private JLabel[] calibrationPoints = new JLabel[CALIBRATION_POINTS];
    private final int MILISECONDS_BETWEEN_POINTS = 2000;
    private JWindow crosshairWindow = new CrosshairWindow();

    public Calibrator() throws IOException {
        //Create calibration points
        JPanel grid = new JPanel(new GridLayout(3, 3));
        BufferedImage calibrationPoint = null;
        try {
            calibrationPoint =
                Calibrator.getBufferedImage("calibration_point.png");
        } catch (IOException | URISyntaxException e) {
            throw new IOException("Could not load calibration_point.png.");
        }
        for (int i = 0; i < CALIBRATION_POINTS; ++i) {
            calibrationPoints[i] = new JLabel(
                    new ImageIcon(calibrationPoint));
            calibrationPoints[i].setVisible(false);
            grid.add(calibrationPoints[i]);
        }
        getContentPane().add(grid);

        setUndecorated(true);
        setAlwaysOnTop(true);
        setResizable(false);
    }

    public void calibrate() throws CalibrationException {
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
            URL fileUrl = bundle.getEntry("res/" + resource_name);
            result = ImageIO.read(
                    new File(FileLocator.resolve(fileUrl).toURI()));
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

    public void moveCrosshair(int screenX, int screenY) {
        crosshairWindow.setLocation(screenX, screenY);
    }

    public void displayCrosshair(boolean enabled) {
        crosshairWindow.setVisible(enabled);
    }
}
