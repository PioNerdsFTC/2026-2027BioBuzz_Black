package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorSparkFunOTOS;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Hardware.Odometer;
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


    static{
        Scheduler.addTask("init", object -> {
            launcherNectar = Globals.depend("launcherNectar");
            launcherPollen = Globals.depend("launcherPollen");

            elapsedTime = Scheduler.time;

            puppetDriver = new PuppetDriver();
            odometer = Hardware.odometer;
        });
    }


    public static void launchNectar() {
        // Enable the nectar launcher
        launcherNectar.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }


    public static void launchNectar(double speed){
        // Enable the nectar launcher
        launcherNectar.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }

    public static void stopLaunchingNectar(){
        // Disable the nectar launcher
        launcherNectar.disableLauncher();

        // Disable the feeding servo for the nectar launcher
        Storage.disableNectarServo();
    }

    public static void launchPollen() {
        // Enable the nectar launcher
        launcherPollen.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }

    public static void launchPollen(double speed) {
        // Enable the nectar launcher
        launcherPollen.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }

    public static void stopLaunchingPollen() {
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


    // Returns true when the robot is within the accepted tolerance amount of ticks laterally and axially
    public static boolean driveToPosition(double x, double y, double t){
        double Δx = (odometer.x / 365.75 * 100) - x;
        double Δy = (odometer.y / 365.75 * 100) - y;

        puppetDriver.setX(smoothenSimulatedJoystick(Δx));
        puppetDriver.setY(smoothenSimulatedJoystick(Δy));

        return (Δx <= t && Δy <= t);
    }

    // Returns true when the robot is within the accepted tolerance amount of ticks laterally and axially
    public static boolean driveToRotation(double θ, double t){
        double Δθ = (Hardware.yaw + Math.PI) - θ;

        puppetDriver.setRotation(smoothenSimulatedJoystick(Δθ));

        return (Δθ <= t);
    }

    private static double smoothenSimulatedJoystick(double x){
        double ans = Math.signum(x) * 0.5 * Math.pow(Math.abs(x), 2);

        if(ans > 1.0){
            return 1.0;
        } else {
            return ans;
        }
    }
}
