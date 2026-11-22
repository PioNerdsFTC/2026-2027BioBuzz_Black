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


    /*
     * Motion control. Translation runs a PID on the straight-line distance to the waypoint and
     * drives along the unit vector toward it, so the path stays straight while the speed ramps
     * up (slew limit), cruises, and eases into the target (P + D). Heading has its own PID, used
     * both to turn in place and to hold heading while translating.
     *
     * Translation gains are in power per cm, heading gains in power per radian.
     */
    public static final PIDController translationPid = new PIDController(0.025, 0.002, 0.003, 0.06);
    public static final PIDController headingPid = new PIDController(1.0, 0.05, 0.06, 0.06);

    static {
        translationPid.maxOutput = 0.8;
        translationPid.maxRatePerSec = 1.6;   // 0 -> 0.8 power in 0.5 s
        translationPid.integralZone = 10;     // cm; only trims out the last bit of error
        translationPid.integralLimit = 0.1;

        headingPid.maxOutput = 0.6;
        headingPid.maxRatePerSec = 2.0;
        headingPid.integralZone = Math.toRadians(15);
        headingPid.integralLimit = 0.1;
    }

    private static double lastMotionMs = Double.NaN;

    /** Seconds since the previous motion update; resets with {@link #resetMotion()}. */
    private static double motionDt() {
        double now = Scheduler.time.milliseconds();
        double dt = Double.isNaN(lastMotionMs) ? 0.02 : (now - lastMotionMs) / 1000.0;
        lastMotionMs = now;
        // A long gap (e.g. after actions ran) would make the derivative and integral jump.
        return Math.min(Math.max(dt, 1e-3), 0.1);
    }

    /** Call when the target changes or the robot stops being driven, so the next move starts from rest. */
    public static void resetMotion() {
        translationPid.reset();
        headingPid.reset();
        lastMotionMs = Double.NaN;
    }

    /**
     * Drive toward a field position, holding {@code holdHeading} if it is not null.
     *
     * @param x target field x in centimetres
     * @param y target field y in centimetres
     * @param t tolerance in centimetres
     * @param holdHeading heading to hold while driving, in radians, or null to not correct it
     */
    public static boolean driveToPosition(double x, double y, double t, Double holdHeading){
        double dt = motionDt();

        double Δx = x - Coordination.xCm();
        double Δy = y - Coordination.yCm();
        double distance = Math.hypot(Δx, Δy);

        // Stop pushing once inside half the tolerance, so it doesn't hunt around the target.
        translationPid.deadzone = t / 2;
        double power = translationPid.update(distance, dt);

        double forward = 0, right = 0;
        if (distance > 1e-6) {
            double[] robot = Coordination.fieldToRobot(Δx / distance, Δy / distance);
            forward = robot[0] * power;
            right = robot[1] * power;
        }

        puppetDriver.setRotation(
            holdHeading == null ? 0 : headingPid.update(Coordination.headingError(holdHeading), dt)
        );

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

    public static boolean driveToPosition(double x, double y, double t) {
        return driveToPosition(x, y, t, null);
    }

    public static boolean driveToRotation(double θ, double t) {
        double Δθ = Coordination.headingError(θ);

        headingPid.deadzone = t / 2;
        puppetDriver.setRotation(headingPid.update(Δθ, motionDt()));

        return (Math.abs(Δθ) <= t);
    }
}
