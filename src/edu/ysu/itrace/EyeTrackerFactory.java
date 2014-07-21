package edu.ysu.itrace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.ysu.itrace.trackers.*;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;
import fj.data.Option;
import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * Constructs IEyeTracker instances.
 */
public class EyeTrackerFactory {
    private static Option<TrackerType> trackerTypeToBuild = none();

    public enum TrackerType {
        SYSTEM_MOUSE_TRACKER,
        TOBII_TRACKER,
        EYETRIBE,
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
        result.put(TrackerType.EYETRIBE, "Eyetribe");
        return result;
    }

    /**
     * Set the type of eye tracker which will be used for future calls to
     * {@link #getConcreteEyeTracker() getConcreteEyeTracker()}.
     * @param type Type value of eye tracker.
     */
    public static void setTrackerType(TrackerType type) {
        trackerTypeToBuild = some(type);
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
    public static Option<IEyeTracker> getConcreteEyeTracker() throws
            EyeTrackerConnectException, IOException {
        if (trackerTypeToBuild.isSome()) {
            switch (trackerTypeToBuild.some()) {
            case SYSTEM_MOUSE_TRACKER:
                return some((IEyeTracker) new SystemMouseTracker());
            case TOBII_TRACKER:
                return some((IEyeTracker) new TobiiTracker());
            case EYETRIBE:
            	return some((IEyeTracker) new EyetribeTracker());
            default:
                return none();
            }
        } else {
            return none();
        }
    }
}
