package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@TeleOp(name="TeleOp")
public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {

        waitForStart();

        Scheduler.trigger("init:telemetry", telemetry);
        Scheduler.trigger("init:hardware-map", hardwareMap);

        Scheduler.trigger("init", null);

        // Telemetry = Globals.depend("telemetry");

        Globals.add(new Globals.Global("telemetry", telemetry));

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }
    }
}
