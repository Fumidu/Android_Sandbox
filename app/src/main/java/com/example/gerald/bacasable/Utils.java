package com.example.gerald.bacasable;

/**
 * Created by Gérald on 26/01/2017.
 */

public class Utils {

    /**
     * Clamp a value between min and max
     * WARNING : for performance, no check that min < max is performed !
     * @param min min value
     * @param max max value
     * @param value value to be clamped
     * @return min if value lt min, max if value gt max, value otherwise
     */
    public static float Clamp(float min, float max, float value) {
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    /**
     * Perform an afine transformation for a value in one range to another range
     * @param r1Start
     * @param r1Stop
     * @param r2Start
     * @param r2Stop
     * @param val
     * @return
     */
    public static float ChangeRange(float r1Start, float r1Stop, float r2Start, float r2Stop, float val)
    {
        float r1Range = r1Stop - r1Start;
        float r2Range = r2Stop - r2Start;
        return (val - r1Start) / r1Range * r2Range + r2Start;
    }
}
