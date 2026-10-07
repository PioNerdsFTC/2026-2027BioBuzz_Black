package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@Autonomous(name="Auto")
public class Auto extends LinearOpMode {
    @Override
    public void runOpMode() {
        Parameters.currentOperatingEnvironment = "AUTO";

        Scheduler.trigger("pre-init", null);

        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Scheduler.trigger("init", null);

        Logger.log("--- Initialized ---");

        while (!isStarted() && Scheduler.continueRunning) {
            Scheduler.tickHook();
            idle();
        }

        Scheduler.trigger("pre-run", null);

        while (opModeIsActive() && Scheduler.continueRunning) {
            Scheduler.tickHook();
        }

        Logger.log("--- Exiting ---");
        Scheduler.trigger("exit", null);
    }
}
