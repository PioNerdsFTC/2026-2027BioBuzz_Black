package org.pionerds.ftc.teamcode.Drivetrain;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Drivetrain {
    public static OmniDrivetrain omni = new OmniDrivetrain();

    private static boolean permitDriving = true;
    private static boolean permitRotating = true;

    public static void setPermitDriving(boolean allow){permitDriving = allow;}
    public static void setPermitRotating(boolean allow){permitRotating = allow;}

    private static boolean isLeftRotationNegative = true;

    /** Rotation error wrapped to [-pi, pi]. Positive means the target is to the robot's left. */
    public static double getRotationError(double target, double current){
        double twoPi = 2 * Math.PI;
        double error = (target - current) % twoPi;

        if (error > Math.PI) error -= twoPi;
        if (error < -Math.PI) error += twoPi;

        return error;
    }

    /**
     * Turn toward a target heading, slowing down as the robot approaches it.
     * Uses the same stepped speeds as Autonomous's translation.
     */
    public static void rotateToward(double target, double current, double tolerance){
        double error = getRotationError(target, current);
        double magnitude = Math.abs(error);

        if (magnitude <= tolerance) {
            stopRotating();
            return;
        }

        if (magnitude >= 0.50) OmniDrivetrain.rotationSpeed = 0.50;
        else if (magnitude >= 0.20) OmniDrivetrain.rotationSpeed = 0.20;
        else OmniDrivetrain.rotationSpeed = 0.10;

        OmniDrivetrain.flipDirection = error > 0;
        OmniDrivetrain.turning = true;
    }

    public static void stopRotating(){
        OmniDrivetrain.turning = false;
        OmniDrivetrain.rotationSpeed = OmniDrivetrain.DEFAULT_ROTATION_SPEED;
    }

//    public static void driveRobot() {
//        if (permitRotating) {
//            return;
//        }
//
//        if (permitDriving) {
//            OmniDrivetrain.fieldCentricDrive();
//            return;
//        }
//    }

    static {
        AtomicReference<UUID> tick = new AtomicReference<>();

        Scheduler.addTask("turn-left:down", (obj2) -> {
            OmniDrivetrain.rotationSpeed = OmniDrivetrain.DEFAULT_ROTATION_SPEED;
            OmniDrivetrain.flipDirection = isLeftRotationNegative;
            OmniDrivetrain.turning = true;
        });

        Scheduler.addTask("turn-left:up", (obj2) -> {
            OmniDrivetrain.turning = false;
        });

        Scheduler.addTask("turn-right:down", (obj2) -> {
            OmniDrivetrain.rotationSpeed = OmniDrivetrain.DEFAULT_ROTATION_SPEED;
            OmniDrivetrain.flipDirection = !isLeftRotationNegative;
            OmniDrivetrain.turning = true;
        });

        Scheduler.addTask("turn-right:up", (obj2) -> {
            OmniDrivetrain.turning = false;
        });

        Scheduler.addTask("init", (obj) -> {
            if (!Environment.Capabilities.drivetrain) return;

            OmniDrivetrain.setMotors(
               Hardware.mapping.getMotor("motor0"),
               Hardware.mapping.getMotor("motor1"),
               Hardware.mapping.getMotor("motor2"),
               Hardware.mapping.getMotor("motor3")
            );

            tick.set(Scheduler.addTask((obj2) -> {
                if (!Scheduler.running) return;

                OmniDrivetrain.fieldCentricDrive();
            }));

            Logger.log("Initialized drivetrain");
        });

        Scheduler.addTask("exit", (obj) -> {
            if (tick.get() != null) Scheduler.removeTask(tick.get());
            tick.set(null);

            OmniDrivetrain.nonEmergencyStop();
        });
    }
}
