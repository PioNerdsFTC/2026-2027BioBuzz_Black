package org.pionerds.ftc.teamcode.Coordination;


import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Vision.Vision;

import java.util.HashMap;

public class Coordination {
    public static double x;
    public static double y;

    /**
     * Field dimensions in centimeters divided by 100 (our dimensions are in x/100)
     */
    public final double UNIT = 365.75 / 100;

    static {
        Scheduler.addTask("init", (obj) -> {
            // TODO: implement robot starting positions

            Coordination.x = 50;
            Coordination.y = 50;
        });
    }

    public static void updateFromVector(double deltaX, double deltaY) {
        Coordination.x+=deltaX;
        Coordination.y+=deltaY;
    }
}
