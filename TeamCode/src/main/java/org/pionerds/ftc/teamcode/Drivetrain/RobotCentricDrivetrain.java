package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class RobotCentricDrivetrain {
    private static final DcMotorEx[] driveMotors = {null, null, null, null};
    private static final double[] drivePowers = {0.00, 0.00, 0.00, 0.00};

    private static Driver driver;

    // Initialization
    static {
        Scheduler.addTask("init", obj -> {
            if(Environment.Capabilities.useFieldCentricDriving) return;

            driveMotors[0] = Hardware.mapping.getMotor("drive0");
            driveMotors[1] = Hardware.mapping.getMotor("drive1");
            driveMotors[2] = Hardware.mapping.getMotor("drive2");
            driveMotors[3] = Hardware.mapping.getMotor("drive3");

            for(DcMotorEx motor : driveMotors){
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            }


            // driver1 only exists in tele, otherwise fall back to auto puppet
            driver = Globals.has("driver1")
                    ? Globals.depend("driver1") : null;


            Scheduler.addTask(obj2 -> {
                fetchRequestedMovement();
                tickDrivetrain();
            });

        });
    }

    private static double axial = 0.00;
    private static double lateral = 0.00;
    private static double yaw = 0.00;

    private static void fetchRequestedMovement() {
        axial = Math.signum(driver.leftStickY()) * Math.pow(driver.leftStickY(), 2);
        lateral = Math.signum(driver.leftStickX()) * Math.pow(driver.leftStickX(), 2);
        yaw = Math.signum(driver.rightStickX()) * Math.pow(driver.rightStickX(), 2);
    }


    public static void tickDrivetrain(){
        drivePowers[0] = (axial - lateral) - yaw;
        drivePowers[1] = (axial - lateral) + yaw;
        drivePowers[2] = axial + lateral + yaw;
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
            if(drivePowers[i] > max) max = drivePowers[i];
        }

        if(max > 1.00){
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
