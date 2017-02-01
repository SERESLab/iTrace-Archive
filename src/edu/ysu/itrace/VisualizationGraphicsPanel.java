package edu.ysu.itrace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class VisualizationGraphicsPanel extends ViewPart implements PaintListener {

	@Override
	public void createPartControl(Composite parent) {
		Canvas graphicsCanvas = new Canvas(parent, SWT.NONE);
		graphicsCanvas.addPaintListener(this);

	}

	@Override
	public void setFocus() {
		//Do nothing.
	}

	@Override
	public void paintControl(PaintEvent pe) {
		pe.gc.setBackground(new Color(pe.gc.getDevice(),100,150,200));
		pe.gc.fillOval(0, 0, 500, 500);
	}

}
