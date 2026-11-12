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

    public static double initialX = -1;
    public static double initialY = -1;

    public static double rotation = 0;

    public static double UNIT = ((double) 1 / 100 * 365.75);

    static {
        Scheduler.addTask("init", (obj) -> {
            // TODO: implement robot starting positions

            if (Environment.Parameters.alliance == Environment.Parameters.Side.RED) {
                Coordination.initialX = 0;
                Coordination.initialY = 85;
            } else {
                Coordination.initialX = 100;
                Coordination.initialY = 15;
            }

            Coordination.x = Coordination.initialX;
            Coordination.y = Coordination.initialY;

            Coordination.rotation = 0;

            Scheduler.addTask(200, (obj2) -> {
                updateRemotes();
            });
        });

        Scheduler.addTask((obj) -> {
            if (Environment.Capabilities.otos) {
                Coordination.x = Coordination.initialX + Hardware.odometer.x;
                Coordination.y = Coordination.initialY + Hardware.odometer.y;
            }
        });

        Scheduler.addTask("exit", (obj) -> {
            Coordination.x = -1;
            Coordination.y = -1;
            Coordination.rotation = -1;
        });
    }

    private static void updateRemotes() {
        Scheduler.trigger("coordination:update-position", null);

        Scheduler.addTask(200, (obj) -> {
            updateRemotes();
        });
    }
}