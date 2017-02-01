package edu.ysu.itrace;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.ui.PlatformUI;

public class StyledTextPaintListener implements PaintListener {
	
	IEventBroker eventBroker;
	
	public StyledTextPaintListener(){
		eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
	}
	
	@Override
	public void paintControl(PaintEvent arg0) {
		eventBroker.post("StyledTextPaintEvent", null);

	}

}
