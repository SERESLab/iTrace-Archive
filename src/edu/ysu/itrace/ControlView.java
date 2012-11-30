package edu.ysu.itrace;

import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import edu.ysu.itrace.jni.EyeTracker;

/**
 * ViewPart for controlling the plugin.
 */
public class ControlView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		
		EyeTracker tracker = new EyeTracker();
		Point2D fixation = tracker.getFixation();
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("Fixation point: " + fixation.toString());
	}

	@Override
	public void setFocus() {
	}
}
