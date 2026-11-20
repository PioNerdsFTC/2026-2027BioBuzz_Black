package org.pionerds.ftc.teamcode.Coordination;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

/**
 * Field dimensions are 365.75 cm x 365.75 cm, however we convert to a 0-100 unit.
 */
public class Coordination {

    public static double x = -1;
    public static double y = -1;

    public static double rotation = 0;

    public static double UNIT = ((double) 1 / 100) * 365.75;

    private static double startX = -1;
    private static double startY = -1;
    /** Field heading at the start pose; the odometer's axes are the robot's axes at that moment. */
    private static double startHeading = 0;

    /** Maps the gyro's reset heading to the heading programmed for the start of the run. */
    private static double rotationOffset = 0;

    /**
     * Odometer reading at the start pose. The OTOS reports centimetres in the robot's axes as they
     * were when tracking was reset (x to the right, y forward), not field coordinates.
     */
    private static double odomXOrigin = 0;
    private static double odomYOrigin = 0;

    static {
        Scheduler.addTask("init", obj -> {
            // TODO: implement robot starting positions

            if (
                Environment.Parameters.alliance ==
                Environment.Parameters.Side.RED
            ) {
                Coordination.x = 0;
                Coordination.y = 85;
            } else {
                Coordination.x = 100;
                Coordination.y = 15;
            }

            Coordination.rotation = 0;
            rotationOffset = 0;
            startHeading = 0;
            odomXOrigin = Hardware.odometer.x;
            odomYOrigin = Hardware.odometer.y;

            startX = Coordination.x;
            startY = Coordination.y;

            Scheduler.addTask(200, obj2 -> {
                updateRemotes();
            });
        });

        Scheduler.addTask(obj -> {
            Coordination.rotation = normalizeAngle(
                rotationOffset - Hardware.yaw
            );

            if (Environment.Capabilities.otos) updatePosition();
        });

        Scheduler.addTask("exit", obj -> {
            Coordination.x = -1;
            Coordination.y = -1;
            Coordination.rotation = -1;
            rotationOffset = 0;
            startHeading = 0;
            odomXOrigin = 0;
            odomYOrigin = 0;
        });
    }

    /**
     * Converts the odometer's displacement into field coordinates.
     *
     * <p>The OTOS resets to the robot's pose, so its readings are in centimetres along the robot's
     * axes at the start: +x is the robot's right, +y is its front. Rotating that by the start
     * heading gives a field displacement, which is added to the programmed start position.
     */
    private static void updatePosition() {
        double right = (Hardware.odometer.x - odomXOrigin) / UNIT;
        double forward = (Hardware.odometer.y - odomYOrigin) / UNIT;

        double cos = Math.cos(startHeading);
        double sin = Math.sin(startHeading);

        Coordination.x = startX - right * sin + forward * cos;
        Coordination.y = startY + right * cos + forward * sin;
    }

    /**
     * Override the starting position, e.g. with one programmed from the dashboard. Call after init.
     */
    public static void setStart(double x, double y) {
        setStart(x, y, null);
    }

    /**
     * Override the starting position and heading, e.g. with one programmed from the dashboard.
     * The heading is in radians and is mapped onto the gyro's reset heading.
     */
    public static void setStart(double x, double y, Double rotation) {
        Coordination.x = x;
        Coordination.y = y;

        rotationOffset = rotation == null ? 0 : rotation + Hardware.yaw;
        Coordination.rotation = rotation == null ? 0 : rotation;

        startX = x;
        startY = y;
        startHeading = rotation == null ? 0 : rotation;

        odomXOrigin = Hardware.odometer.x;
        odomYOrigin = Hardware.odometer.y;
    }

    /**
     * Converts a field-frame vector (e.g. waypoint minus current position) into the robot's own
     * axes using the current heading. Inverse of the rotation in {@link #updatePosition}, but with
     * the live heading rather than the start heading, since the robot may have turned since.
     *
     * @return {forward, right} in the same units as the input
     */
    public static double[] fieldToRobot(double dx, double dy) {
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        return new double[] { dx * cos + dy * sin, -dx * sin + dy * cos };
    }

    /**
     * Signed shortest turn from the current heading to {@code target}, in (-π, π]. Measured in
     * the same frame as {@link #rotation}: positive means the field heading must increase, which
     * is a clockwise turn since {@code rotation = rotationOffset - yaw}.
     */
    public static double headingError(double target) {
        return normalizeAngle(target - rotation);
    }

    private static double normalizeAngle(double angle) {
        double twoPi = 2 * Math.PI;

        angle %= twoPi;
        if (angle > Math.PI) angle -= twoPi;
        if (angle < -Math.PI) angle += twoPi;

        return angle;
    }

    private static void updateRemotes() {
        Scheduler.trigger("coordination:update-position", null);

        Scheduler.addTask(200, obj -> {
            updateRemotes();
        });
    }
}
