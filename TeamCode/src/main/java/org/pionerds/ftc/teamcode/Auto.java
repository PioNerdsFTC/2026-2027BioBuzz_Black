package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@Autonomous(name="Auto")
public class Auto extends LinearOpMode {
    @Override
    public void runOpMode() {
        Parameters.currentOperatingEnvironment = "AUTO";
        Parameters.running = false;

        Scheduler.trigger("pre-init", null);

        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Scheduler.trigger("init", null);

        while (!isStarted()) {
            Scheduler.tickHook();
            idle();
        }

        Parameters.running = true;

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }

        Parameters.running = false;
        Scheduler.trigger("exit", null);
    }
}
