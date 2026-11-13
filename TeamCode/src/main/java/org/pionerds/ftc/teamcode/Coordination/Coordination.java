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

    public static double UNIT = ((double) 1 / 100 * 365.75);

    private static double startX = -1;
    private static double startY = -1;

    /** Maps the gyro's reset heading to the heading programmed for the start of the run. */
    private static double rotationOffset = 0;

    static {
        Scheduler.addTask("init", (obj) -> {
            // TODO: implement robot starting positions

            if (Environment.Parameters.alliance == Environment.Parameters.Side.RED) {
                Coordination.x = 0;
                Coordination.y = 85;
            } else {
                Coordination.x = 100;
                Coordination.y = 15;
            }

            Coordination.rotation = 0;
            rotationOffset = 0;

            startX = Coordination.x;
            startY = Coordination.y;

            Scheduler.addTask(200, (obj2) -> {
                updateRemotes();
            });
        });

        Scheduler.addTask((obj) -> {
            // Gyro yaw is positive counter-clockwise; the dashboard/field heading is positive clockwise.
            Coordination.rotation = normalizeAngle(rotationOffset - Hardware.yaw);

            if (Environment.Capabilities.otos) {
                Coordination.x = 50 + Hardware.odometer.x;
                Coordination.y = 50 + Hardware.odometer.y;
            }
        });

        Scheduler.addTask("exit", (obj) -> {
            Coordination.x = -1;
            Coordination.y = -1;
            Coordination.rotation = -1;
            rotationOffset = 0;
        });
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

        Scheduler.addTask(200, (obj) -> {
            updateRemotes();
        });
    }
}
