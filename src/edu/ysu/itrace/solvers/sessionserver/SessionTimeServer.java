package edu.ysu.itrace.solvers.sessionserver;

import edu.ysu.itrace.gaze.IGazeResponse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SessionTimeServer implements ISessionTimeServer, EventHandler {
    public static int SERVER_PORT = 44555;
    
    private final List<Socket> clients;
    private final SessionTimeServerThread thread;
    
    public SessionTimeServer() {
        clients = Collections.synchronizedList(new ArrayList());
        thread = new SessionTimeServerThread(SERVER_PORT, this);
    }
    
    public void config(String sessionID, String devUsername) {
        // Do nothing
    }
    
    public void displayExportFile() {
    	// Do nothing
    }
    
    public void dispose() {
        this.stop();
    }

    public String friendlyName() {
        return "Session Time Server";
    }
    
    public List<Socket> getClients() {
        return clients;
    }

    public void init() {
        this.start();
    }
    
    public void process(IGazeResponse response) {
        this.emitTime(response.getGaze().getSessionTime());
    }
    
	public void handleEvent(Event event) {
		String[] propertyNames = event.getPropertyNames();
		IGazeResponse response = (IGazeResponse)event.getProperty(propertyNames[0]);
		this.process(response);
	}
    
    public void start() {
        synchronized(clients) {
            clients.clear();
        }
        thread.start();
    }
    
    public void emitTime(long time) {
        synchronized(clients) {
            Iterator<Socket> i = clients.iterator();
            while(i.hasNext()) {
                Socket socket = i.next();
                
                if(socket.isClosed()) {
                    i.remove();
                    continue;
                }
                
                try {
                	ByteBuffer buffer = ByteBuffer.allocate(8);
                	buffer.order(ByteOrder.LITTLE_ENDIAN);
                	buffer.putLong(time);
                	socket.getOutputStream().write(buffer.array());
                	socket.getOutputStream().flush();
                } catch(IOException e) {
                    try {
                        socket.close();
                    } catch(IOException e2) {
                        e2.printStackTrace();
                    }
                    i.remove();
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void stop() {
        synchronized(clients) {
            Iterator<Socket> i = clients.iterator();
            while(i.hasNext()) {
                try {
                    i.next().close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                i.remove();
            }
        }
        thread.setShouldStop(true);
    }
}
