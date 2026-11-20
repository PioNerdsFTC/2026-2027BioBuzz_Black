package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Drivetrain {
    private static final DcMotorEx[] driveMotors = {null, null, null, null};
    private static final double[] drivePowers = {0.00, 0.00, 0.00, 0.00};

    private static final double MAX_MOTOR_SPEED = 1;

    private static Driver driver;

    // Initialization
    static {
        AtomicReference<UUID> tick = new AtomicReference<>();

        Scheduler.addTask("init", obj -> {
            if(!Environment.Capabilities.useFieldCentricDriving) return;

            driveMotors[0] = Hardware.mapping.getMotor("motor0");
            driveMotors[1] = Hardware.mapping.getMotor("motor1");
            driveMotors[2] = Hardware.mapping.getMotor("motor2");
            driveMotors[3] = Hardware.mapping.getMotor("motor3");

            driveMotors[0].setDirection(DcMotorSimple.Direction.REVERSE);
            driveMotors[1].setDirection(DcMotorSimple.Direction.FORWARD);
            driveMotors[2].setDirection(DcMotorSimple.Direction.FORWARD);
            driveMotors[3].setDirection(DcMotorSimple.Direction.FORWARD);

            for(DcMotorEx motor : driveMotors){
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            }


            // driver1 only exists in tele, otherwise fall back to auto puppet
            driver = Globals.has("driver1")
                    ? Globals.depend("driver1") : null;

            tick.set(
                Scheduler.addTask(obj2 -> {
                    if (!Scheduler.running || driver == null) return;
                    fetchRequestedMovement();
                    tickDrivetrain();
                })
            );

        });

        Scheduler.addTask("exit", obj -> {
            if(!Environment.Capabilities.useFieldCentricDriving) return;

            if (tick.get() != null) Scheduler.removeTask(tick.get());
            tick.set(null);

            drivePowers[0] = 0.00;
            drivePowers[1] = 0.00;
            drivePowers[2] = 0.00;
            drivePowers[3] = 0.00;

            updateMotors();
        });

    }

    private static double axial = 0.00;
    private static double lateral = 0.00;
    private static double yaw = 0.00;
    private static double robotAngle = 0.00;
    private static double stickAngle = 0.00;
    private static double headingAngle = 0.00;

    private static void fetchRequestedMovement() {
        robotAngle = Hardware.yaw;
        stickAngle = Math.atan2(driver.leftStickY(), driver.leftStickX());

        headingAngle = robotAngle - stickAngle;

        double newX = Math.cos(headingAngle);
        double newY = Math.sin(headingAngle);
        double magnitude = Math.sqrt(Math.pow(driver.leftStickX(), 2) + Math.pow(driver.leftStickY(), 2));

        axial = newX * magnitude;
        lateral = newY * magnitude;
        yaw = driver.rightStickX();




    }


    public static void tickDrivetrain(){
        drivePowers[0] = (axial - lateral) - yaw;
        drivePowers[2] = (axial - lateral) + yaw;
        drivePowers[1] = axial + lateral + yaw;
        drivePowers[3] = (axial + lateral) - yaw;

        normalizeMotors();
        updateMotors();
    }

    private static void updateMotors(){
        for(int i = 0; i< driveMotors.length; i++){
            driveMotors[i].setPower(drivePowers[i]);
        }

    }


    private static void normalizeMotors(){
        double max = 0.00;

        for(int i = 0; i< driveMotors.length; i++){
            if(Math.abs(drivePowers[i]) > max) max = Math.abs(drivePowers[i]);
        }

        if(max > 1.0){
            for(int i = 0; i< driveMotors.length; i++){
                drivePowers[i] /= max;
            }
        }
    }

    // Allow the Autonomous to Puppet the Drivetrain
    public static void overrideDriver(Driver newDriver) {
        driver = newDriver;
    }

}
