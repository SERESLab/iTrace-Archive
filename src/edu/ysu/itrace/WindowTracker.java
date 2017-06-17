package edu.ysu.itrace;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import edu.ysu.itrace.ITrace;


public class WindowTracker implements ShellListener{

	Shell shell;
	String title;
	
	public WindowTracker(Shell s, String t)
	{
		shell = s;
		title = t;	
	}
	
	@Override
	public void shellActivated(ShellEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shellClosed(ShellEvent arg0) {
		// TODO Auto-generated method stub	
		ITrace def = ITrace.getDefault();
		def.removeWindow();	
	}

	@Override
	public void shellDeactivated(ShellEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shellDeiconified(ShellEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shellIconified(ShellEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	
	
	
	
}
