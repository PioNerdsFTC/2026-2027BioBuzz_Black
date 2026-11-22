package org.pionerds.ftc.teamcode.Coordination;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

/**
 * Field dimensions are 365.75 cm x 365.75 cm, however we convert to a 0-100 unit.
 */
public class Coordination {

    /** Field width and depth in centimetres. */
    public static final double FIELD_CM = 365.75;

    /** Centimetres per Coordination unit, i.e. the size of 1% of the field. */
    public static final double UNIT = FIELD_CM / 100;

    /** The dashboard works in Coordination units (0-100); the odometer reports centimetres. */
    public static double unitsToCm(double units) {
        return units * UNIT;
    }

    public static double cmToUnits(double cm) {
        return cm / UNIT;
    }

    public static double x = -1;
    public static double y = -1;

    public static double rotation = 0;

    /** Current field position in centimetres. */
    public static double xCm() {
        return unitsToCm(x);
    }

    public static double yCm() {
        return unitsToCm(y);
    }

    private static double startX = -1;
    private static double startY = -1;

    /**
     * Field heading the robot had when the gyro and odometer were reset. The odometer's axes are
     * fixed to the robot's axes at that moment, so this is what turns an odometer displacement
     * into a field displacement. It is also the offset that maps the gyro's zero onto the field.
     */
    private static double rotationOffset = 0;

    /**
     * Odometer reading at the start pose. The OTOS reports centimetres in the robot's axes as they
     * were when tracking was reset (x forward, y to the left), not field coordinates.
     */
    private static double odomXOrigin = 0;
    private static double odomYOrigin = 0;

    /** Here to reduce how many logging calls are made. */
    private static int logIteration = 0;

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

            if (Environment.Capabilities.otos) {
                updatePosition();

                if (Environment.Parameters.competing) return;

                logIteration++;

                if (logIteration >= 20) {
                    Logger.log("X: " + x + ", Y: " + y + ", R: " +  rotation);
                    logIteration = 0;
                }
            }
        });

        Scheduler.addTask("exit", obj -> {
            Coordination.x = -1;
            Coordination.y = -1;
            Coordination.rotation = -1;
            rotationOffset = 0;
            odomXOrigin = 0;
            odomYOrigin = 0;
        });
    }

    /**
     * Converts the odometer's displacement into field coordinates.
     *
     * <p>The OTOS resets its tracking frame when the gyro resets, so its readings are in
     * centimetres along the robot's axes at that moment: +x is the robot's front, +y is its
     * left. The defining field heading of those axes is {@link #rotationOffset}, not the start
     * heading: the robot may be rotated between init and the start pose. Rotating the
     * displacement by that heading gives a field displacement, which is added to the start
     * position.
     */
    private static void updatePosition() {
        // The OTOS reports centimetres; convert them into the dashboard's 0-100 units.
        // SparkFun OTOS axes: +x is the robot's front, +y is its LEFT (not x-right / y-forward).
        double forward = cmToUnits(Hardware.odometer.x - odomXOrigin);
        double right = -cmToUnits(Hardware.odometer.y - odomYOrigin);

        double cos = Math.cos(rotationOffset);
        double sin = Math.sin(rotationOffset);

        Coordination.x = startX - right * sin + forward * cos;
        Coordination.y = startY + right * cos + forward * sin;
    }

    /**
     * Override the starting position, e.g. with one programmed from the dashboard. Call at the
     * start pose, after the robot has been placed.
     */
    public static void setStart(double x, double y) {
        setStart(x, y, null);
    }

    /**
     * Override the starting position and heading, e.g. with one programmed from the dashboard.
     * The heading is in radians and is mapped onto the gyro's reset heading. Call at the start
     * pose, not during init: this also captures the odometry origin.
     */
    public static void setStart(double x, double y, Double rotation) {
        Coordination.x = x;
        Coordination.y = y;

        // The gyro zeroed when the odometer reset, so the field heading at that moment is the
        // start heading plus however far the robot has turned since.
        rotationOffset = normalizeAngle(
            rotation == null ? Hardware.yaw : rotation + Hardware.yaw
        );
        Coordination.rotation = rotation == null ? 0 : rotation;

        startX = x;
        startY = y;

        odomXOrigin = Hardware.odometer.x;
        odomYOrigin = Hardware.odometer.y;
    }

    /**
     * Converts a field-frame vector (e.g. waypoint minus current position) into the robot's own
     * axes using the current heading. Inverse of the rotation in {@link #updatePosition}, but with
     * the live heading rather than the odometer-reset heading, since the robot may have turned
     * since.
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
