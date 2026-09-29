package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@TeleOp(name="Tele")
public class Tele extends LinearOpMode {
    @Override
    public void runOpMode() {
        Parameters.currentOperatingEnvironment = "AUTO";

        Scheduler.trigger("pre-init", null);

        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Scheduler.trigger("init", null);

        while (!isStarted() && !Scheduler.continueRunning) {
            Scheduler.tickHook();
            idle();
        }

        Scheduler.trigger("pre-run", null);

        while (opModeIsActive() && !Scheduler.continueRunning) {
            Scheduler.tickHook();
        }

        Scheduler.trigger("exit", null);
    }
}
