package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class OmniDrivetrain {

    private static Driver driver;
    private static Driver overrideDriver;
    private static DcMotorEx[] motors = {null, null, null, null}; //front right, front left, back left, back right
    private static final double[] motorSpeed = {0.0, 0.0, 0.0, 0.0};
    private static final double rotationSpeed = 1.0;

    public static boolean flipDirection = false;
    public static boolean turning = false;

    static {
        Scheduler.addTask("init", (obj) -> {
            // driver1 only exists in tele, otherwise fall back to auto puppet 
            driver = Globals.has("driver1") ? Globals.depend("driver1") : overrideDriver;
        });
    }

    public static void OVERRIDE_DRIVER(Driver driver){
        OmniDrivetrain.overrideDriver = driver;
        OmniDrivetrain.driver = driver;
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
        if (driver == null) {
            nonEmergencyStop();
            return;
        }

        double x = driver.leftStickX();
        double y = driver.leftStickY();
        double gyroAngle = Hardware.yaw;

        double F = 0.00;
        double S = 0.00;
        double T = 0.00;

        // Bumper turning (full speed) takes priority over proportional right stick turning
        if (turning) {
            T = rotationSpeed * (flipDirection ? -1.00 : 1.00);
        } else {
            double r = driver.rightStickX();

            if (Math.abs(r) >= 0.05) T = r * rotationSpeed;
        }

        if (Math.abs(x) >= 0.05 || Math.abs(y) >= 0.05) {
            double[] fieldCentricPositionRequest = getFieldCentricPositionRequest(x, y, gyroAngle);

            double magnitude = Math.min(fieldCentricPositionRequest[2], 1.00);

            S = fieldCentricPositionRequest[0] * magnitude;
            // Gamepad y is negative when pushed forward, so flip it to make F positive = forward
            F = -fieldCentricPositionRequest[1] * magnitude;
        }

        motorSpeed[0] = F - S - T;  // front right
        motorSpeed[1] = F + S + T;  // front left
        motorSpeed[2] = F - S + T;  // back left
        motorSpeed[3] = F + S - T;  // back right

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
            if (motors[i] == null) continue;

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
