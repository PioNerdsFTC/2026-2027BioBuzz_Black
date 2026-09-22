package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Input.BulkReading;

import java.util.EmptyStackException;

@TeleOp(name="TeleOp")
public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Scheduler.trigger("init", new Object());

        waitForStart();

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }
    }
}
