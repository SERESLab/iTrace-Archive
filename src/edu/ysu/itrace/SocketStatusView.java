package edu.ysu.itrace;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SocketStatusView extends ViewPart implements EventHandler, PaintListener {
	private Canvas canvas;
	private String data = "";
	private IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
	
	@Override
	public void createPartControl(Composite parent) {
		eventBroker.subscribe("SocketData", this);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(this);
		
	}

	@Override
	public void setFocus() {
		//Do nothing.

	}

	@Override
	public void handleEvent(Event event) {
		String[] names = event.getPropertyNames();
		data = (String)event.getProperty(names[0]);
		canvas.redraw();
		
	}

	@Override
	public void paintControl(PaintEvent pe) {
		pe.gc.drawText(data, 0, 0);
		
	}

}
