package edu.ysu.itrace.gaze;

import java.util.Map;

import edu.ysu.itrace.Gaze;

/**
 * Defines a response to a gaze event. Returned by objects implementing
 * IGazeHandler.
 */
public interface IGazeResponse {

    /**
     * Returns the name of the artifact under the gaze.
     */
    public String getName();

    /**
     * Returns the type of artifact.
     */
    public String getType();

    /**
     * Returns a name,value pair of properties specific to the artifact type.
     */
    public Map<String,String> getProperties();

    /**
     * Returns the gaze object from which the response originated.
     */
    public Gaze getGaze();
}
