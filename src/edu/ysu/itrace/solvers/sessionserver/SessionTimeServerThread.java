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
            } finally {
                listener.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        this.setShouldStop(false);
    }
}
