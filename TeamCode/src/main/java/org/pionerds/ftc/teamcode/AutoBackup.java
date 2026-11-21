package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.RobotCentricDrivetrain;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

@Autonomous(name = "Auto-Backup")
public class AutoBackup extends LinearOpMode {

    @Override
    public void runOpMode() {
        Environment.Parameters.operatingEnvironment =
                Environment.Parameters.OperatingEnvironment.AUTO;

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


        // Add code
        PuppetDriver puppetDriver = new PuppetDriver();
        RobotCentricDrivetrain.overrideDriver(puppetDriver);

        puppetDriver.setX(0.50);
        puppetDriver.setY(0.00);

        sleep(2000);

        puppetDriver.setX(0.00);
        puppetDriver.setY(0.50);

        sleep(2000);

        puppetDriver.setX(0.00);
        puppetDriver.setY(0.00);
        puppetDriver.setRotation(1.00);

        sleep(2000);

        puppetDriver.setX(0.00);
        puppetDriver.setY(0.00);
        puppetDriver.setRotation(0.00);

        sleep(2000);


        while (opModeIsActive() && Scheduler.continueRunning) {
            Scheduler.tickHook();

            sleep(500);
            puppetDriver.setRotation(0.50);
            sleep(500);
            puppetDriver.setRotation(-0.50);
        }

        Logger.log("--- Exiting ---");
        Scheduler.trigger("exit", null);

        Environment.Parameters.operatingEnvironment =
                Environment.Parameters.OperatingEnvironment.NONE;
    }
}
