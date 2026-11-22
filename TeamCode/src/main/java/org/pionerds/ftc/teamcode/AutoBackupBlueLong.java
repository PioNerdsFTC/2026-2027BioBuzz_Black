package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.RobotCentricDrivetrain;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Storage.Launcher;
import org.pionerds.ftc.teamcode.Storage.Storage;

@Autonomous(name = "Auto-Backup-BlueLong")
public class AutoBackupBlueLong extends LinearOpMode {

    @Override
    public void runOpMode() {
        Environment.Parameters.operatingEnvironment =
                Environment.Parameters.OperatingEnvironment.AUTO;
        Environment.Parameters.emergencyAuto = true;

        try {
            Scheduler.trigger("pre-init", null);

            Globals.add("telemetry", telemetry);
            Globals.add("hardware-map", hardwareMap);

            Scheduler.trigger("init", null);

            Logger.log("--- Initialized ---");

            while (!isStarted() && Scheduler.continueRunning) {
                Scheduler.tickHook();
                idle();
            }

            Launcher launcherNectar = null;
            Launcher launcherPollen = null;

            if(Environment.Capabilities.launchers){
                launcherNectar = Globals.depend("launcherNectar");
                launcherPollen = Globals.depend("launcherPollen");
            }

            Scheduler.trigger("pre-run", null);

            // Add code
            PuppetDriver puppetDriver = new PuppetDriver();
            RobotCentricDrivetrain.overrideDriver(puppetDriver);

            // Drive forward
            puppetDriver.setX(0.00);
            puppetDriver.setY(0.50);
            driveFor(400);

            // Stop driving forward
            puppetDriver.setX(0.00);
            puppetDriver.setY(0.00);
            driveFor(1000);

            // Spin Launchers Up
            launcherPollen.enableLauncher(1020);
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Move pollen up
            Storage.enablePollenServo();
            Storage.enableIntake();
            driveFor(1000);

            // Pause launching
            Storage.disablePollenServo();
            Storage.disableIntake();
            driveFor(1000);

            // Shut things off
            launcherPollen.disableLauncher();
            Storage.disableNectarServo();

            driveFor(600);

            puppetDriver.setX(-0.5);
            puppetDriver.setY(0.00);
            driveFor(2250);

            puppetDriver.setX(0.00);
            puppetDriver.setY(0.00);
            driveFor(500);

            puppetDriver.setX(0.00);
            puppetDriver.setY(0.00);
            puppetDriver.setRotation(-0.15);
            driveFor(500);

            puppetDriver.setX(0.00);
            puppetDriver.setY(0.5);
            puppetDriver.setRotation(0.00);
            driveFor(3000);

            puppetDriver.setX(-0.50);
            puppetDriver.setY(0.00);
            driveFor(2000);

            puppetDriver.setX(0.00);
            puppetDriver.setY(0.00);
            driveFor(1000);

            // Stop
            stopDrivetrain(puppetDriver);

            Logger.log("--- Exiting ---");
        } finally {
            Scheduler.trigger("exit", null);

            Environment.Parameters.operatingEnvironment =
                    Environment.Parameters.OperatingEnvironment.NONE;
            Environment.Parameters.emergencyAuto = false;
        }
    }

    /**
     * Runs the scheduler (and therefore the drivetrain) for the given duration instead of sleeping.
     * A plain sleep does not tick, so commanded movement would never reach the motors.
     */
    private void driveFor(long milliseconds) {
        long end = System.currentTimeMillis() + milliseconds;

        while (
                opModeIsActive() &&
                        Scheduler.continueRunning &&
                        System.currentTimeMillis() < end
        ) {
            Scheduler.tickHook();
        }
    }

    /** Command zero movement, then tick once so the motors are actually set to zero. */
    private void stopDrivetrain(PuppetDriver puppetDriver) {
        puppetDriver.setX(0.00);
        puppetDriver.setY(0.00);
        puppetDriver.setRotation(0.00);

        Scheduler.tickHook();
    }
}
