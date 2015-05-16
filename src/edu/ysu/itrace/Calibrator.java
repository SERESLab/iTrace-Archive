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
            } catch (IOException e) {
                System.out.println("Failed to load crosshair icon. IOException");
            } catch (URISyntaxException e) {
                System.out.println("Failed to load crosshair icon. URISyntaxException");
            }
        }

        public void setLocation(int x, int y) {
            super.setLocation(x - centre.x, y - centre.y);
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
            URL fileUrl = bundle.getEntry("res/" + resourceName);System.out.println(FileLocator.resolve(fileUrl));
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
