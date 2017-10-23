package edu.ysu.itrace.solvers.sessionserver;

import edu.ysu.itrace.solvers.ISolver;
import java.net.Socket;
import java.util.List;

public interface ISessionTimeServer extends ISolver {
    List<Socket> getClients();
    
    void start();
    
    void emitTime(long time);
    
    void stop();
}
