package org.pionerds.ftc.teamcode.Drivetrain;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class _Drivetrain {

    public static _OmniDrivetrain omni = new _OmniDrivetrain();

    /** Turn power per radian of heading error. */
    private static final double ROTATION_GAIN = 1.20;
    /** Lowest power that still overcomes friction, so the robot keeps moving just outside tolerance. */
    private static final double MIN_ROTATION_SPEED = 0.08;

    private static boolean isLeftRotationNegative = true;

    /** Rotation error wrapped to [-pi, pi]. Positive means the target is clockwise from the current heading. */
    public static double getRotationError(double target, double current) {
        double twoPi = 2 * Math.PI;
        double error = (target - current) % twoPi;

        if (error > Math.PI) error -= twoPi;
        if (error < -Math.PI) error += twoPi;

        return error;
    }

    /**
     * Turn toward a target heading, slowing down as the robot approaches it.
     *
     * <p>Headings here are {@link org.pionerds.ftc.teamcode.Coordination.Coordination#rotation},
     * which is clockwise positive, and a positive turn power is also clockwise, so a positive
     * error needs a positive turn.
     */
    public static void rotateToward(
        double target,
        double current,
        double tolerance
    ) {
        double error = getRotationError(target, current);
        double magnitude = Math.abs(error);

        if (magnitude <= tolerance) {
            stopRotating();
            return;
        }

        _OmniDrivetrain.rotationSpeed = Math.max(
            MIN_ROTATION_SPEED,
            Math.min(1.00, magnitude * ROTATION_GAIN)
        );

        // Negative error means the target is counter-clockwise, which needs a negative turn
        _OmniDrivetrain.flipDirection = error < 0;
        _OmniDrivetrain.turning = true;
    }

    public static void stopRotating() {
        _OmniDrivetrain.turning = false;
        _OmniDrivetrain.flipDirection = false;
        _OmniDrivetrain.rotationSpeed = _OmniDrivetrain.DEFAULT_ROTATION_SPEED;
    }

    static {
        AtomicReference<UUID> tick = new AtomicReference<>();

        Scheduler.addTask("turn-left:down", obj2 -> {
            _OmniDrivetrain.rotationSpeed =
                _OmniDrivetrain.DEFAULT_ROTATION_SPEED;
            _OmniDrivetrain.flipDirection = isLeftRotationNegative;
            _OmniDrivetrain.turning = true;
        });

        Scheduler.addTask("turn-left:up", obj2 -> {
            _OmniDrivetrain.turning = false;
        });

        Scheduler.addTask("turn-right:down", obj2 -> {
            _OmniDrivetrain.rotationSpeed =
                _OmniDrivetrain.DEFAULT_ROTATION_SPEED;
            _OmniDrivetrain.flipDirection = !isLeftRotationNegative;
            _OmniDrivetrain.turning = true;
        });

        Scheduler.addTask("turn-right:up", obj2 -> {
            _OmniDrivetrain.turning = false;
        });

        Scheduler.addTask("init", obj -> {
            if (!Environment.Capabilities.drivetrain) return;

            _OmniDrivetrain.setMotors(
                Hardware.mapping.getMotor("motor0"),
                Hardware.mapping.getMotor("motor1"),
                Hardware.mapping.getMotor("motor2"),
                Hardware.mapping.getMotor("motor3")
            );

            // Re-register on every init so a restart cannot leave two drive ticks running
            if (tick.get() != null) Scheduler.removeTask(tick.get());

            tick.set(
                Scheduler.addTask(obj2 -> {
                    if (!Scheduler.running) return;

                    _OmniDrivetrain.tick();
                })
            );

            Logger.log("Initialized drivetrain");
        });

        Scheduler.addTask("exit", obj -> {
            if (tick.get() != null) Scheduler.removeTask(tick.get());
            tick.set(null);

            _OmniDrivetrain.nonEmergencyStop();
        });
    }
}
