package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorSparkFunOTOS;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Hardware.Odometer;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Storage.Launcher;
import org.pionerds.ftc.teamcode.Storage.Storage;

public class AutonomousActions {

    public static boolean isLaunchingNectar;
    public static double launchingTimeNectar;
    public static boolean isLaunchingPollen;
    public static double launchingTimePollen;
    public static Launcher launcherNectar;
    public static Launcher launcherPollen;

    public static ElapsedTime elapsedTime;

    public static PuppetDriver puppetDriver;
    public static Odometer odometer;

    static {
        Scheduler.addTask("init", object -> {
            if (Environment.Parameters.emergencyAuto) return;

            try {
                launcherNectar = Globals.depend("launcherNectar");
                launcherPollen = Globals.depend("launcherPollen");
            } catch(Exception e) {
                Logger.error("Launchers not available to Auto; scoring utterly disabled");
                Environment.Capabilities.launchers = false;
            }

            elapsedTime = Scheduler.time;

            puppetDriver = new PuppetDriver();
            odometer = Hardware.odometer;
        });
    }


    public static void launchNectar() {
        if(!Environment.Capabilities.launchers) return;
        // Enable the nectar launcher
        launcherNectar.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }


    public static void launchNectar(double speed){
        if(!Environment.Capabilities.launchers) return;
        // Enable the nectar launcher
        launcherNectar.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }

    public static void stopLaunchingNectar(){
        if(!Environment.Capabilities.launchers) return;
        // Disable the nectar launcher
        launcherNectar.disableLauncher();

        // Disable the feeding servo for the nectar launcher
        Storage.disableNectarServo();
    }

    public static void launchPollen() {
        if(!Environment.Capabilities.launchers) return;
        // Enable the nectar launcher
        launcherPollen.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }

    public static void launchPollen(double speed) {
        if(!Environment.Capabilities.launchers) return;
        // Enable the nectar launcher
        launcherPollen.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }

    public static void stopLaunchingPollen() {
        if(!Environment.Capabilities.launchers) return;
        // Disable the nectar launcher
        launcherPollen.disableLauncher();

        // Disable the feeding servo for the nectar launcher
        Storage.disablePollenServo();
    }

    private static double delayStart = 0.00;
    private static double delayTime = 0.00;
    public static void delay(double milliseconds) {
        delayStart = elapsedTime.milliseconds();
        delayTime = milliseconds;
    }

    public static boolean delay() {
        return elapsedTime.milliseconds() - delayStart >= delayTime;
    }

    public static void enableIntake(){
        Storage.enableIntake();
    }

    public static void disableIntake(){
        Storage.disableIntake();
    }


    /**
     * @param x target field x in centimetres
     * @param y target field y in centimetres
     * @param t tolerance in centimetres
     */
    public static boolean driveToPosition(double x, double y, double t){
        double Δx = x - Coordination.xCm();
        double Δy = y - Coordination.yCm();

        double[] robot = Coordination.fieldToRobot(Δx, Δy);
        double forward = robot[0];
        double right = robot[1];

        if (Environment.Capabilities.useFieldCentricDriving) {
            double left = -right;
            double cos = Math.cos(Hardware.yaw);
            double sin = Math.sin(Hardware.yaw);

            puppetDriver.setX(forward * cos - left * sin);
            puppetDriver.setY(forward * sin + left * cos);
        } else {
            puppetDriver.setX(right);
            puppetDriver.setY(forward);
        }

        return (Math.abs(Δx) <= t && Math.abs(Δy) <= t);
    }

    public static boolean driveToRotation(double θ, double t) {
        double Δθ = Coordination.headingError(θ);

        puppetDriver.setRotation(Δθ);

        return (Math.abs(Δθ) <= t);
    }
}
