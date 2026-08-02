package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Scheduler.Scheduler;

public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        Scheduler.addTask(new Scheduler.Task(() -> {}, 1000));
        Scheduler.addTask(new Scheduler.Task(() -> {
            Logger.log("asdf");
        }, "init"));
        Scheduler.trigger("init");

        waitForStart();

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }
    }
}
