package edu.ysu.itrace;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class EngineSocket{
	private Socket socket;
	private BufferedReader reader;
	private String data = "";
	private Timer timer;
	private IEventBroker eventBroker;
	
	
	EngineSocket(){
		timer = new Timer();
		eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
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
							int end = data.length()-1;
							//while(end-1 > 0 && (int)data.charAt(end-1) < 128) end--;
							//int startingIndex = data.indexOf("iTraceData");
							//if(startingIndex != -1){
								//data = data.substring(startingIndex, data.length());
								eventBroker.post("SocketData", data);
							//}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}, 0,10);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

}
