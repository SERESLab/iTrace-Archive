package edu.ysu.itrace.solvers.sessionserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class SessionTimeServerThread extends Thread {
    public static final int BACKLOG = 10;
    
    private boolean shouldStop = false;
    private int port;
    private ISessionTimeServer sessionServer;
    
    SessionTimeServerThread(int port, ISessionTimeServer sessionServer) {
        this.port = port;
        this.sessionServer = sessionServer;
    }
    
    void setShouldStop(boolean shouldStop) {
        this.shouldStop = shouldStop;
    }
    
    public void run() {
        while(true) {
            try {
                ServerSocket listener = new ServerSocket(port, SessionTimeServerThread.BACKLOG);
                try {
                    while(true) {
                        if(shouldStop) {
                            break;
                        }

                        Socket conn = listener.accept();
                        sessionServer.getClients().add(conn);
                    }
                } catch(IOException e2) {
                    e2.printStackTrace();
                }
            	System.out.println("Session Time Server listener closed.");
                listener.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            while(shouldStop) {
            	// TODO: Look into why wait/notify caused issues with the Affectiva app being unable to connect every other time tracking was started.
            }
        }
    }
}
