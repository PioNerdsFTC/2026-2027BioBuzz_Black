package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class OmniDrivetrain {

    private static Driver driver;
    private static DcMotorEx[] motors = {null, null, null, null}; //front right, front left, back left, back right
    private static final double[] motorSpeed = {0.0, 0.0, 0.0, 0.0};
    private static final double rotationSpeed = 1.0;

    public static boolean flipDirection = false;
    public static boolean turning = false;

    static {
        Scheduler.addTask("init", (obj) -> {
            driver = Globals.depend("driver1");
        });
    }

    public static void setMotors(
            DcMotorEx motor0,
            DcMotorEx motor1,
            DcMotorEx motor2,
            DcMotorEx motor3
    ) {
        motors = new DcMotorEx[] { motor0, motor1, motor2, motor3 };
    }

    public static void fieldCentricDrive(){
        double x = driver.leftStickX();
        double y = driver.leftStickY();
        double gyroAngle = Hardware.yaw;

        // Tolerance-Dead Zone
        if (Math.abs(x) < 0.2 && Math.abs(y) < 0.2) {
            Logger.log("Inside Dead Zone (Driving)");
            nonEmergencyStop();
            return;
        }

        double[] fieldCentricPositionRequest = getFieldCentricPositionRequest(x, y, gyroAngle);

        x = fieldCentricPositionRequest[0];
        y = fieldCentricPositionRequest[1];

        double magnitude = fieldCentricPositionRequest[2];

        motorSpeed[0] = (x + y);
        motorSpeed[1] = (x - y);
        motorSpeed[2] = (-x - y);
        motorSpeed[3] = (-x + y);

        motorSpeed[0] *= magnitude;
        motorSpeed[1] *= magnitude;
        motorSpeed[2] *= magnitude;
        motorSpeed[3] *= magnitude;
        Logger.log("DRIVING ROBOT");

        if (turning) {
            Logger.log("TURNING ROBOT");
            double rotSpeed = rotationSpeed;

            if (flipDirection) rotSpeed = -rotSpeed;

            motorSpeed[0] += rotSpeed;
            motorSpeed[1] += rotSpeed;
            motorSpeed[2] += rotSpeed;
            motorSpeed[3] += rotSpeed;
        }

        normalizeMotors();
        updateMotors();
    }

    // Returns an array of [x,y] that the robot should be moving toward. Factoring in the robot's angle. Only God can recreate this method.
    public static double[] getFieldCentricPositionRequest(double x, double y, double robotOrientation){
        x = -x;
        y = -y;

        double mag = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        double joystickAngle = Math.atan2(y, x);
        double finalAngle = joystickAngle + robotOrientation;

        x = Math.cos(finalAngle);
        y = Math.sin(finalAngle);

        return new double[] { -x, -y, mag };
    }

    public static void robotCentricDrive() {
        double x = driver.leftStickX();
        double y = driver.leftStickY();

        // Tolerance-Dead Zone
        if (Math.abs(x) < 0.2 && Math.abs(y) < 0.2) {
            nonEmergencyStop();
            return;
        }

        motorSpeed[0] = (-x - y);
        motorSpeed[1] = (-x + y);
        motorSpeed[2] = (x + y);
        motorSpeed[3] = (x - y);
    }

    public static void updateMotors() {
        for (int i = 0; i < 4; i++) {
            motors[i].setPower(motorSpeed[i]);
        }
    }

    public static void normalizeMotors() {
        double max = 0.00;

        for (int i = 0; i < 4; i++) {
            double speed = Math.abs(motorSpeed[i]);

            if (speed > max) max = speed;
        }

        if (max <= 1.00) return;

        motorSpeed[0] /= max;
        motorSpeed[1] /= max;
        motorSpeed[2] /= max;
        motorSpeed[3] /= max;
    }

    public static void emergencyStop() {
        motorSpeed[0] = 0.00;
        motorSpeed[1] = 0.00;
        motorSpeed[2] = 0.00;
        motorSpeed[3] = 0.00;

        updateMotors();

        // Spawn new thread here to lock motors from being updated.
    }

    public static void nonEmergencyStop() {
        motorSpeed[0] = 0.00;
        motorSpeed[1] = 0.00;
        motorSpeed[2] = 0.00;
        motorSpeed[3] = 0.00;

        updateMotors();
    }
}