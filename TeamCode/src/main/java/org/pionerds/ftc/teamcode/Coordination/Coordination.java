package org.pionerds.ftc.teamcode.Coordination;

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
        });

        Scheduler.addTask("exit", (obj) -> {
            Coordination.x = -1;
            Coordination.y = -1;
            Coordination.rotation = -1;
        });
    }

    /**
     *
     * @param deltaX X updates (in cm)
     * @param deltaY Y updates (in cm)
     */
    public static void updateFromVector(double deltaX, double deltaY) throws Exception {
        if (x == -1 || y == -1 || Coordination.rotation == -1) throw new Exception("Called updateFromVector before initialization");

        Coordination.x += deltaX * UNIT;
        Coordination.y += deltaY * UNIT;
    }
}
