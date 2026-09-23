package org.pionerds.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Input.BulkReading;

@TeleOp(name="TeleOp")
public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        Scheduler.trigger("pre-init", null);

        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Scheduler.trigger("init", null);

        while (!isStarted()) {
            Scheduler.tickHook();
            idle();
        }

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }

        Scheduler.trigger("exit", null);
    }
}
