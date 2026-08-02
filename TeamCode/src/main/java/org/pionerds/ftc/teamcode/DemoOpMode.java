package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Scheduler.Scheduler;

public class DemoOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        Scheduler.trigger("init");
        Scheduler.addTask(new Scheduler.Task(() -> {}, 1000));

        waitForStart();

        while (opModeIsActive()) {
            Scheduler.tickHook();
        }
    }
}
