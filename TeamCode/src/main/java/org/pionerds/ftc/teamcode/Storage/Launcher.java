package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Vision.Vision;

public class Launcher {

    private String name;
    private DcMotorEx launchMotor;
    private Servo hoodServo;

    private boolean isEnabled = false;

    private static double HOOD_DEGREES = 68.0;

    private static final double LAUNCH_VELOCITY_STEP = 50.0;
    private static final double MAX_LAUNCH_VELOCITY = 3000.0;

    private double[] launcherOffsetFromCamera = new double[2];
    private double hiveHeightMeters = 1.4605;

    public Launcher(
            String name,
            String motorName,
            String launchServoName,
            boolean reverseDirection
    ) {
        launchMotor = Hardware.mapping.getMotor(motorName);
        if (launchMotor != null && reverseDirection) launchMotor.setDirection(
                DcMotorSimple.Direction.REVERSE
        );

        //hoodServo = Hardware.mapping.getServoMotor(launchServoName);

        if (launchMotor != null) {
            launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            launchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        /*UUID rightTrigger = Scheduler.addTask("right-trigger", trigger -> {
            setLaunchAngle((Float) trigger * 180);
        });*/

        final boolean[] launcherToggled = {false};

        UUID toggleLauncher = Scheduler.addTask("toggle-launcher:down", obj -> {
            if (launcherToggled[0]) disableLauncher();
            else enableLauncher();

            launcherToggled[0] = !launcherToggled[0];
        });

        UUID updateLauncher = Scheduler.addTask(obj -> {
            this.updateLaunchVelocity();
        });

        AtomicReference<UUID> exit = new AtomicReference<>();

        exit.set(
                Scheduler.addTask("exit", obj -> {
                    //Scheduler.removeTask(rightTrigger);
                    Scheduler.removeTask(toggleLauncher);
                    Scheduler.removeTask(updateLauncher);
                    Scheduler.removeTask(exit.get());
                })
        );
    }

    public void setLauncherOffsetFromCamera(double[] launcherOffsetFromCamera) {
        this.launcherOffsetFromCamera = launcherOffsetFromCamera;
    }

    /**
     * @param theta hood angle in degrees, 0-180. Servo positions are 0-1.
     */
    /*public void setLaunchAngle(double theta) {
        if (hoodServo != null) hoodServo.setPosition(theta / 180);
    }*/

    private double overrideLaunchSpeed = 0.00;
    private boolean hasOverrideLaunchSpeed = false;

    public void enableLauncher() {
        isEnabled = true;
    }

    public void enableLauncher(double speed) {
        isEnabled = true;
        overrideLaunchSpeed = speed;
        hasOverrideLaunchSpeed = true;
    }

    public void disableLauncher() {
        isEnabled = false;
        overrideLaunchSpeed = 0.00;
        hasOverrideLaunchSpeed = false;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Register "pressed" events that nudge the launcher velocity up and down.
     * Each press changes the target velocity by {@link #LAUNCH_VELOCITY_STEP}.
     */
    public void registerVelocityControls(
            String increaseEvent,
            String decreaseEvent
    ) {
        UUID increase = Scheduler.addTask(increaseEvent, obj ->
                increaseLaunchVelocity()
        );
        UUID decrease = Scheduler.addTask(decreaseEvent, obj ->
                decreaseLaunchVelocity()
        );

        AtomicReference<UUID> exit = new AtomicReference<>();

        exit.set(
                Scheduler.addTask("exit", obj -> {
                    Scheduler.removeTask(increase);
                    Scheduler.removeTask(decrease);
                    Scheduler.removeTask(exit.get());
                })
        );
    }

    public void increaseLaunchVelocity() {
        setOverrideLaunchSpeed(overrideLaunchSpeed + LAUNCH_VELOCITY_STEP);
    }

    public void decreaseLaunchVelocity() {
        setOverrideLaunchSpeed(overrideLaunchSpeed - LAUNCH_VELOCITY_STEP);
    }

    public double getLaunchVelocity() {
        return overrideLaunchSpeed;
    }

    private void setOverrideLaunchSpeed(double speed) {
        overrideLaunchSpeed = Math.max(
                0.00,
                Math.min(speed, MAX_LAUNCH_VELOCITY)
        );
        hasOverrideLaunchSpeed = true;

        Logger.log(
                name + " velocity set to: " + overrideLaunchSpeed
        );
    }

    public void updateLaunchVelocity() {
        if (launchMotor == null) {
            return;
        }

        if (!isEnabled) {
            // Logger.log("LAUNCH VELOCITY SET TO: 0.00");
            launchMotor.setVelocity(0.00);
            return;
        }

        if (Environment.Capabilities.vision || Environment.Capabilities.aimbotEnabled) {
            if (hasOverrideLaunchSpeed) {
                // Logger.log("LAUNCH VELOCITY SET TO: "+overrideLaunchSpeed);
                launchMotor.setVelocity(overrideLaunchSpeed);
                return;
            }

            AprilTagClusterDetection clusterDetection;

            if (Environment.Parameters.alliance == Environment.Parameters.Side.BLUE) {
                clusterDetection = Vision.getClusterTagByName("BLUE SCORING");
            } else {
                clusterDetection = Vision.getClusterTagByName("RED SCORING");
            }

            if (clusterDetection == null || clusterDetection.ftcPose == null) {
                // Cannot see apriltags
                launchMotor.setVelocity(0);
                return;
            }

            double xi = launcherOffsetFromCamera[0];
            double xf = clusterDetection.ftcPose.y * 0.0254;
            double yi = launcherOffsetFromCamera[1];
            double yf = hiveHeightMeters;
            double θ = HOOD_DEGREES * Math.PI / 180.0;

            // Logger.log("xi = " + xi);
            // Logger.log("xf = " + xf);
            // Logger.log("yi = " + yi);
            // Logger.log("fi = " + yf);
            // Logger.log("theta = " + θ);

            // Logger.warn("LAUNCH VELOCITY SET TO: " + Aimbot.getAngularVelocity(xi, xf, yi, yf, θ));
            // Logger.warn("LAUNCH VELOCITY GET IS: " + launchMotor.getVelocity());

            try {
                launchMotor.setVelocity(Aimbot.getAngularVelocity(xi, xf, yi, yf, θ));
            } catch(Exception e) {
                // Ignore. Aimbot will throw sometimes ~~
            }
        }
    }
}
