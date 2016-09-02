package edu.ysu.itrace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.ysu.itrace.trackers.*;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;

/**
 * Constructs IEyeTracker instances.
 */
public class EyeTrackerFactory {
    private static TrackerType trackerTypeToBuild = null;

    public enum TrackerType {
        SYSTEM_MOUSE_TRACKER,
        TOBII_TRACKER,
        EYEX_TRACKER
    }

    /**
     * Get the classes of each available eye tracker interface.
     * @return Map of tracker types and their pretty printed names. Interation
     *     order is predictable.
     */
    public static Map<TrackerType, String> getAvailableEyeTrackers() {
        Map<TrackerType, String> result
            = new LinkedHashMap<TrackerType, String>();
        result.put(TrackerType.SYSTEM_MOUSE_TRACKER, "System Mouse Tracker");
        result.put(TrackerType.TOBII_TRACKER, "Tobii Tracker");
        result.put(TrackerType.EYEX_TRACKER, "EyeX Tracker");
        return result;
    }

    /**
     * Set the type of eye tracker which will be used for future calls to
     * {@link #getConcreteEyeTracker() getConcreteEyeTracker()}.
     * @param type Type value of eye tracker.
     */
    public static void setTrackerType(TrackerType type) {
        trackerTypeToBuild = type;
    }

    /**
     * Builds an interface and connects to an eye tracker specified by
     * {@link #setTrackerType(TrackerType) setTrackerType()}.
     * @return Some IEyeTracker if successful, none if no tracker type or an
     *     invalid tracker type has been set.
     * @throws EyeTrackerConnectException if the IEyeTracker fails to connect
     *     to the tracker device.
     * @throws IOException if any I/O errors occur while constructing the
     *     IEyeTracker.
     */
    public static IEyeTracker getConcreteEyeTracker() throws
            EyeTrackerConnectException, IOException {
        if (trackerTypeToBuild != null) {
            switch (trackerTypeToBuild) {
            case SYSTEM_MOUSE_TRACKER:
                return (IEyeTracker) new SystemMouseTracker();
            case TOBII_TRACKER:
                return (IEyeTracker) new TobiiTracker();
            case EYEX_TRACKER:
            	return (IEyeTracker) new EyeXTracker();
            default:
                return null;
            }
        } else {
            return null;
        }
    }
}
