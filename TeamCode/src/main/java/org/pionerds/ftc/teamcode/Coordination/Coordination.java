package org.pionerds.ftc.teamcode.Coordination;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.HashMap;


/**
 * Field dimensions are 365.75 cm x 365.75 cm, however we convert to a 0-100 unit.
 */
public class Coordination {
    public static double x = -1;
    public static double y = -1;

    public static double rotation = 0;

    public static double UNIT = ((double) 1 /100 * 365.75);

    static {
        Scheduler.addTask("init", (obj) -> {
            // TODO: implement robot starting positions

            Coordination.x = 50;
            Coordination.y = 50;
            Coordination.rotation = 0;

            Scheduler.addTask(200, (obj2) -> {
                updateRemotes();
            });
        });

        Scheduler.addTask((obj) -> {
            if (Environment.Capabilities.otos) {
                Coordination.x = 50 + Hardware.odometer.x;
                Coordination.y = 50 + Hardware.odometer.y;
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