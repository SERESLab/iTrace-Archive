package edu.ysu.itrace;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class EngineSocket implements PaintListener {
	Socket socket;
	BufferedReader reader;
	String data = "";
	Timer timer;
	Shell shell;
	
	EngineSocket(){
		timer = new Timer();
		
		try{
			socket = new Socket("localhost", 8080);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(socket.isConnected());
			
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					
					try {
						if(reader.ready()){
							data = reader.readLine();
							int end = 0;
							//while(end != data.length() && (int)data.charAt(end) != 0) end++;
							//if(end != data.length()) 
							if(data.length() >= 9)
								data = data.substring(data.length()-9, data.length());
							//System.out.println(data);
							PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable(){

								@Override
								public void run() {
									shell.redraw();
									
								}
								
							});
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}, 0,10);
			shell = new Shell(SWT.DOUBLE_BUFFERED);
			shell.setSize(1000,100);
			shell.addPaintListener(this);
			shell.setVisible(true);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void paintControl(PaintEvent pe) {
		pe.gc.drawText(data, 0, 0);
		
	}
}
