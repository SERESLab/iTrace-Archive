package edu.ysu.itrace.solvers;

import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Defines a minimal interface for passing data to a solver.
 */
public interface ISolver {
    // This interface is intended to provide only general
    // functionality needed in the event loop. Additional
    // features, such as retrieving results, should be
    // defined in another, more specific, interface.

    /**
     * A name of the solver suitable to display to the user.
     */
    public String friendlyName();

    /**
     * Launch a configuration dialog.
     */
    public void config();

    /**
     * Any initialization work with side effects, such as opening files. This
     * method should very probably be called before calling process or dispose.
     */
    public void init();

    /**
     * Called to process new gazes.
     */
    public void process(IGazeResponse response);

    /**
     * Frees any resources. It is very likely a bad idea to process new data
     * after calling dispose. Not sure if we need this, either.
     */
    public void dispose();
}
