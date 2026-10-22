package org.pionerds.ftc.teamcode.Drivetrain;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Drivetrain {
    static Driver driver;
    public static OmniDrivetrain omni = new OmniDrivetrain();

    private static boolean permitDriving = true;
    private static boolean permitRotating = true;

    public static void setPermitDriving(boolean allow){permitDriving = allow;}
    public static void setPermitRotating(boolean allow){permitRotating = allow;}

    private static boolean isLeftRotationNegative = true;

    public static void driveRobot() {
        if (permitRotating) {
            return;
        }

        if (permitDriving) {
            OmniDrivetrain.fieldCentricDrive();
            return;
        }
    }

    static {
        AtomicReference<UUID> tick = new AtomicReference<>();

        AtomicReference<UUID> onTurnLeft = new AtomicReference<>();
        AtomicReference<UUID> onTurnLeftUp = new AtomicReference<>();
        AtomicReference<UUID> onTurnRight = new AtomicReference<>();
        AtomicReference<UUID> onTurnRightUp = new AtomicReference<>();

        Scheduler.addTask("init", (obj) -> {

            driver = Globals.depend("driver1");

            OmniDrivetrain.setMotors(
                    Hardware.mapping.getMotor("motor0"),
                    Hardware.mapping.getMotor("motor1"),
                    Hardware.mapping.getMotor("motor2"),
                    Hardware.mapping.getMotor("motor3")
            );

            tick.set(Scheduler.addTask((obj2) -> {
                OmniDrivetrain.fieldCentricDrive();
            }));

            onTurnLeft.set(Scheduler.addTask("turn-left:down", (obj2) -> {
                OmniDrivetrain.flipDirection = isLeftRotationNegative;
                OmniDrivetrain.turning = true;
                Logger.log("turn-left");
            }));

            onTurnLeftUp.set(Scheduler.addTask("turn-left:up", (obj2) -> {
                OmniDrivetrain.turning = false;
            }));

            onTurnRight.set(Scheduler.addTask("turn-right:down", (obj2) -> {
                OmniDrivetrain.flipDirection = !isLeftRotationNegative;
                OmniDrivetrain.turning = true;
                Logger.log("turn-right");
            }));

            onTurnRightUp.set(Scheduler.addTask("turn-right:up", (obj2) -> {
                OmniDrivetrain.turning = false;
            }));

            Logger.log("Initializing drivetrain");
        });

        Scheduler.addTask("exit", (obj) -> {
            if (tick.get() != null) {
                Scheduler.removeTask(tick.get());
            }
            if (onTurnRight.get() != null) {
                Scheduler.removeTask(onTurnRight.get());
            }

            if (onTurnLeft.get() != null) {
                Scheduler.removeTask(onTurnLeft.get());
            }
        });
    }
}
