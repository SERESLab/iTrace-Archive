package edu.ysu.itrace;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sets up client queues to retrieve gazes from eye tracker and transports gaze
 * data from the tracker to the queues. Automatically toggles tracking on and
 * off depending on the amount of active clients.
 */
public class GazeTransport extends Thread {
    private volatile boolean running = false;
    private IEyeTracker eyeTracker = null;
    private boolean trackerRunning = false;
    private Vector<LinkedBlockingQueue<Gaze>> clients =
            new Vector<LinkedBlockingQueue<Gaze>>();

    /**
     * Constructor.
     *
     * @param eyeTracker Eye tracking from which to transport gaze data.
     */
    public GazeTransport(IEyeTracker eyeTracker) {
        this.eyeTracker = eyeTracker;
    }

    /**
     * Stops thread.
     */
    public void quit() {
        running = false;
    }

    public void run() {
        running = true;
        while (running) {
            if (trackerRunning)
            {
                //Get up to 15 gazes from the eye tracker per run. Add to all
                //client queues.
                Gaze currentGaze = eyeTracker.getGaze();
                for (int loops = 0; loops < 15 && currentGaze != null;
                        ++loops) {
                    for (LinkedBlockingQueue<Gaze> client : clients)
                        client.add(currentGaze);
                    currentGaze = eyeTracker.getGaze();
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //Who needs sleep anyways?
            }
        }
    }

    /**
     * Creates a new client queue and enables tracking if not yet enabled.
     *
     * @return Client queue or null if fails to start tracking.
     */
    public LinkedBlockingQueue<Gaze> createClient() {
        if (clients.size() == 0) {
            trackerRunning = true;
            try {
                eyeTracker.clear();
                eyeTracker.startTracking();
            } catch (IOException e) {
                return null;
            }
        }

        LinkedBlockingQueue<Gaze> client = new LinkedBlockingQueue<Gaze>();
        clients.add(client);
        return client;
    }

    /**
     * Removes client and disables tracking if no clients remain.
     *
     * @param client Client to remove.
     * @return Success status.
     */
    public boolean removeClient(LinkedBlockingQueue<Gaze> client) {
        if (clients.size() == 1) {
            try {
                eyeTracker.stopTracking();
            } catch (IOException e) {
                return false;
            }
        }

        clients.remove(client);
        return true;
    }
}
