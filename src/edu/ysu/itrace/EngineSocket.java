package edu.ysu.itrace;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class EngineSocket {
	Socket socket;
	BufferedReader reader;
	String data;
	Timer timer;
	
	
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
							System.out.println(data);
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
