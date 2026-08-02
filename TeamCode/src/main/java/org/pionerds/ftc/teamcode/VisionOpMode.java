package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Scheduler.Scheduler;

public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        Scheduler.addTask(new Scheduler.Task((obj) -> {
            Telemetry telemetry = (Telemetry) obj;

            telemetry.addLine("sadf");
            telemetry.update();
        }, "init"));

        waitForStart();

        Scheduler.trigger("init:telemetry", telemetry);
        Scheduler.trigger("init", telemetry);

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }
    }
}
