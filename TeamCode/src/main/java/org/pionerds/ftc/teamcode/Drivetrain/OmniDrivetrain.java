package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

/**
 * Mecanum drivetrain.
 *
 * <p>Sign conventions, which every method here depends on:
 * <ul>
 *     <li>{@code forward} is positive toward the robot's front.</li>
 *     <li>{@code strafe} is positive to the robot's right (matching gamepad left stick X).</li>
 *     <li>{@code turn} is positive clockwise.</li>
 * </ul>
 * {@link Hardware#yaw} is counter-clockwise positive while {@link Coordination#rotation} is
 * clockwise positive, so positive turn power increases {@code Coordination.rotation} and
 * decreases {@code Hardware.yaw}. Mixing those two up is how the robot ends up turning the wrong way.
 */
public class OmniDrivetrain {

    private static Driver driver;
    private static Driver overrideDriver;
    private static DcMotorEx[] motors = { null, null, null, null }; //front right, front left, back left, back right
    private static final double[] motorSpeed = { 0.0, 0.0, 0.0, 0.0 };
    private static final String[] motorNames = {
        "front-right",
        "front-left",
        "back-left",
        "back-right",
    };

    public static final double DEFAULT_ROTATION_SPEED = 1.0;
    public static double rotationSpeed = DEFAULT_ROTATION_SPEED;

    public static boolean flipDirection = false;
    public static boolean turning = false;

    /** Set by emergencyStop: all motor writes are forced to zero until unlock() is called. */
    private static boolean locked = false;

    /** Field-frame velocity requested by autonomous. +x is right on the field, +y is toward the audience. */
    private static double fieldX = 0.00;
    private static double fieldY = 0.00;
    private static boolean fieldVectorActive = false;

    static {
        Scheduler.addTask("init", obj -> {
            // driver1 only exists in tele, otherwise fall back to auto puppet
            driver = Globals.has("driver1")
                ? Globals.depend("driver1")
                : overrideDriver;

            // A fresh run must never inherit an emergency stop from a previous one
            locked = false;
            clearFieldVector();
            resetRotation();
        });

        Scheduler.addTask("exit", obj -> {
            clearDriver();
            clearFieldVector();
            resetRotation();
            nonEmergencyStop();
        });
    }

    public static void OVERRIDE_DRIVER(Driver newDriver) {
        OmniDrivetrain.overrideDriver = newDriver;
        OmniDrivetrain.driver = newDriver;
    }

    public static void clearDriver() {
        OmniDrivetrain.driver = null;
    }

    public static void setMotors(
        DcMotorEx motor0,
        DcMotorEx motor1,
        DcMotorEx motor2,
        DcMotorEx motor3
    ) {
        motors = new DcMotorEx[] { motor0, motor1, motor2, motor3 };

        for (int i = 0; i < motors.length; i++) {
            if (motors[i] == null) {
                Logger.warn(
                    "Drivetrain: " +
                        motorNames[i] +
                        " motor (motor" +
                        i +
                        ") is not mapped"
                );
                continue;
            }

            // FLOAT lets the robot coast, which makes every closed loop overshoot its target
            motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    /**
     * Called once per scheduler tick by {@link Drivetrain}. Picks whichever control source is
     * active and writes the motors, so there is exactly one writer per tick.
     */
    public static void tick() {
        if (locked) {
            updateMotors();
            return;
        }

        if (fieldVectorActive) {
            if (
                Environment.Parameters.operatingEnvironment ==
                Environment.Parameters.OperatingEnvironment.AUTO
            ) {
                fieldVectorDrive();
            } else {
                // A stale request from an autonomous run must never steer teleop
                clearFieldVector();
                fieldCentricDrive();
            }

            return;
        }

        fieldCentricDrive();
    }

    public static void fieldCentricDrive() {
        if (driver == null) {
            nonEmergencyStop();
            return;
        }

        double x = driver.leftStickX();
        double y = driver.leftStickY();

        double forward = 0.00;
        double strafe = 0.00;

        if (Math.abs(x) >= 0.05 || Math.abs(y) >= 0.05) {
            double[] fieldCentricPositionRequest =
                getFieldCentricPositionRequest(x, y, Hardware.yaw);

            double magnitude = Math.min(fieldCentricPositionRequest[2], 1.00);

            strafe = fieldCentricPositionRequest[0] * magnitude;
            // Gamepad y is negative when pushed forward, so flip it to make forward positive
            forward = -fieldCentricPositionRequest[1] * magnitude;
        }

        mix(forward, strafe, turnPower());
        normalizeMotors();
        updateMotors();
    }

    /**
     * Drive without field centering: the sticks act in the robot's own frame.
     * {@code +x = strafe right}, {@code -y = forward}, right stick = turn.
     */
    public static void robotCentricDrive() {
        if (driver == null) {
            nonEmergencyStop();
            return;
        }

        double x = driver.leftStickX();
        double y = driver.leftStickY();

        double forward = 0.00;
        double strafe = 0.00;

        if (Math.abs(x) >= 0.05 || Math.abs(y) >= 0.05) {
            // Gamepad y is negative when pushed forward
            forward = -y;
            strafe = x;
        }

        mix(forward, strafe, turnPower());
        normalizeMotors();
        updateMotors();
    }

    /**
     * Autonomous's control path: a velocity in field coordinates.
     *
     * <p>Autonomous coordinates come from {@link Coordination} (dashboard units, +x toward the
     * blue wall, +y toward the audience). They cannot go through the driver sticks, because those
     * are interpreted relative to the robot's heading when the gyro was reset.
     */
    public static void setFieldVector(double x, double y) {
        fieldX = x;
        fieldY = y;
        fieldVectorActive = true;
    }

    public static void clearFieldVector() {
        fieldX = 0.00;
        fieldY = 0.00;
        fieldVectorActive = false;
    }

    public static boolean hasFieldVector() {
        return fieldVectorActive;
    }

    private static void fieldVectorDrive() {
        double heading = Coordination.rotation; // radians, clockwise positive, 0 = field +x
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);

        // The robot's front points along the heading; its right is 90 degrees clockwise of it
        double forward = fieldX * cos + fieldY * sin;
        double strafe = -fieldX * sin + fieldY * cos;

        mix(forward, strafe, turnPower());
        normalizeMotors();
        updateMotors();
    }

    /**
     * Mecanum mixing. Matches the reference FTC implementation
     * (RobotTeleopMecanumFieldRelativeDrive.drive): front-left pairs with back-right, and
     * front-right pairs with back-left.
     *
     * @param forward positive toward the robot's front
     * @param strafe  positive to the robot's right
     * @param turn    positive clockwise
     */
    private static void mix(double forward, double strafe, double turn) {
        motorSpeed[0] = forward - strafe - turn; // front right
        motorSpeed[1] = forward + strafe + turn; // front left
        motorSpeed[2] = forward - strafe + turn; // back left
        motorSpeed[3] = forward + strafe - turn; // back right
    }

    /** Turn power for this tick: bumper/autonomous turning wins over the right stick. */
    private static double turnPower() {
        if (turning) return rotationSpeed * (flipDirection ? -1.00 : 1.00);

        if (driver == null) return 0.00;

        double r = driver.rightStickX();

        if (Math.abs(r) < 0.05) return 0.00;

        return r * rotationSpeed;
    }

    // Returns an array of [x,y] that the robot should be moving toward. Factoring in the robot's angle. Only God can recreate this method.
    public static double[] getFieldCentricPositionRequest(
        double x,
        double y,
        double robotOrientation
    ) {
        x = -x;
        y = -y;

        double mag = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        double joystickAngle = Math.atan2(y, x);
        double finalAngle = joystickAngle + robotOrientation;

        x = Math.cos(finalAngle);
        y = Math.sin(finalAngle);

        return new double[] { -x, -y, mag };
    }

    public static void updateMotors() {
        for (int i = 0; i < 4; i++) {
            if (motors[i] == null) continue;

            motors[i].setPower(locked ? 0.00 : motorSpeed[i]);
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

    /**
     * Latches every motor to zero. Unlike {@link #nonEmergencyStop()} this stays in effect across
     * ticks until {@link #unlock()} (or the next init) releases it.
     */
    public static void emergencyStop() {
        locked = true;
        zeroMotors();
    }

    /** Releases an emergency stop latch. */
    public static void unlock() {
        locked = false;
    }

    public static boolean isLocked() {
        return locked;
    }

    public static void nonEmergencyStop() {
        zeroMotors();
    }

    private static void zeroMotors() {
        motorSpeed[0] = 0.00;
        motorSpeed[1] = 0.00;
        motorSpeed[2] = 0.00;
        motorSpeed[3] = 0.00;

        updateMotors();
    }

    public static void resetRotation() {
        turning = false;
        flipDirection = false;
        rotationSpeed = DEFAULT_ROTATION_SPEED;
    }
}
