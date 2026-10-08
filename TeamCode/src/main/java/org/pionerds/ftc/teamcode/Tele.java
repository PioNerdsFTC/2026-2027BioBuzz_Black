package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Driver.DriverAction;
import org.pionerds.ftc.teamcode.Driver.DriverInput;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@TeleOp(name="Tele")
public class Tele extends LinearOpMode {
    @Override
    public void runOpMode() {
        Parameters.operatingEnvironment = Parameters.OperatingEnvironment.TELE;

        Scheduler.trigger("pre-init", null);

        Globals.add("telemetry", telemetry);
        Globals.add("hardware-map", hardwareMap);

        Globals.add("gamepad1", gamepad1);
        Globals.add("gamepad2", gamepad2);

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

        Parameters.operatingEnvironment = Parameters.OperatingEnvironment.NONE;
    }
}
