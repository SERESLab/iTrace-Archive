package edu.ysu.itrace;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows eye positions extracted from gaze data.
 */
public class EyeStatusView extends Window {
    /**
     * Timeout-recursive Runnable which redraws the canvas frequently.
     */
    private class RedrawRunnable implements Runnable {
        private Display display = null;
        private Canvas canvas = null;

        public RedrawRunnable(Display display, Canvas canvas) {
            this.display = display;
            this.canvas = canvas;
        }

        public void run() {
            canvas.redraw();
            display.timerExec(10, new RedrawRunnable(display, canvas));
        }
    }

    private final int EYE_CIRCLE_SIZE = 20;
    private Canvas canvas;
    private LinkedBlockingQueue<Gaze> gazeQueue = null;

    public EyeStatusView(Shell parentShell, GazeTransport transport) {
        super(parentShell);
        gazeQueue = transport.createClient();
        //TODO: Close client when done.
    }

    public Control createContents(Composite parent) {
        final Composite composite = parent;
        composite.setLayout(new FillLayout());

        canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
        canvas.addPaintListener(new PaintListener() {
            private Gaze current = null;

            public void paintControl(PaintEvent event) {
                Gaze newGaze = gazeQueue.poll();
                if (newGaze != null)
                    current = newGaze;

                if (current != null) {
                    Point windowSize = composite.getShell().getSize();
                    Display display = composite.getShell().getDisplay();

                    //Left colour based on validity.
                    int leftValidity = (int) (current.getLeftValidity() * 255);
                    event.gc.setBackground(new Color(display,
                            255 - leftValidity, leftValidity, 0));

                    //Left.
                    event.gc.fillOval(
                            (int) (current.getLeftX() * windowSize.x),
                            (int) (current.getLeftY() * windowSize.y),
                            EYE_CIRCLE_SIZE, EYE_CIRCLE_SIZE);
                    event.gc.drawOval(
                            (int) (current.getLeftX() * windowSize.x),
                            (int) (current.getLeftY() * windowSize.y),
                            EYE_CIRCLE_SIZE, EYE_CIRCLE_SIZE);

                    //Right colour based on validity.
                    int rightValidity =
                            (int) (current.getRightValidity() * 255);
                    event.gc.setBackground(new Color(display,
                            255 - rightValidity, rightValidity, 0));

                    //Right.
                    event.gc.fillOval(
                            (int) (current.getRightX() * windowSize.x),
                            (int) (current.getRightY() * windowSize.y),
                            EYE_CIRCLE_SIZE, EYE_CIRCLE_SIZE);
                    event.gc.drawOval(
                            (int) (current.getRightX() * windowSize.x),
                            (int) (current.getRightY() * windowSize.y),
                            EYE_CIRCLE_SIZE, EYE_CIRCLE_SIZE);
                }
            }
        });

        //Repeatedly repaint the canvas on timeout.
        final Display display = getShell().getDisplay();
        (new RedrawRunnable(display, canvas)).run();

        return parent;
    }

    protected Point getInitialSize() {
        return new Point(400, 300);
    }
}
