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

    static {
        AtomicReference<UUID> tick = new AtomicReference<>();

        Scheduler.addTask("init", (obj) -> {
            Logger.log("Initializing drivetrain");

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
        });

        Scheduler.addTask("exit", (obj3) -> {
            if (tick.get() != null) {
                Scheduler.removeTask(tick.get());
            }
        });
    }

    private static boolean permitDriving = true;
    private static boolean permitRotating = true;

    public static void setPermitDriving(boolean allow){permitDriving = allow;}
    public static void setPermitRotating(boolean allow){permitRotating = allow;}

    private static boolean isLeftRotationNegative = false;

    UUID onTurnLeft = Scheduler.addTask("turn-left", (obj) -> {
        OmniDrivetrain.rotateDrive(isLeftRotationNegative);
        Logger.log("turn-left");
    });

    UUID onTurnRight = Scheduler.addTask("turn-right", (obj) -> {
        OmniDrivetrain.rotateDrive(!isLeftRotationNegative);
        Logger.log("turn-right");
    });

    public static void driveRobot(){
        if(permitRotating){
            return;
        }

        if (permitDriving){
            OmniDrivetrain.fieldCentricDrive();
            return;
        }
    }


    // INSERT METHOD HERE FOR CALLING OmniDrive.java.fieldCentricDrive();

    // INSERT METHOD HERE FOR CALLING OmniDrive.java.rotateWithBumpers();

}
