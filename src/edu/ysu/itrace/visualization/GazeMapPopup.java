package edu.ysu.itrace.visualization;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

public class GazeMapPopup extends PopupDialog {
	String 	content;
	Point 	initialLocation;
	
	public GazeMapPopup(VisFixation vf, Point location){
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, false, false, false,
				false, false, null, null);
		initialLocation = location;
		setInfoText(
				"Fixation:" + "\n" +
				"File:" + "\t" + vf.file + "\n" +
				"Line:" + "\t" + vf.line + "\n" +
				"Col:" + "\t" + vf.line + "\n" +
				"dura:" + "\t" + vf.duration
		);
	}
	
	@Override
	protected Point getInitialLocation(Point initialSize){
		return initialLocation;
	}
}
