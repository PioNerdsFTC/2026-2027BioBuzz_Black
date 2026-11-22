package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;

public class Odometer {

    private static final int HEALTH_CHECK_MS = 500;

    SparkFunOTOS otos;

    public double x = 0;
    public double y = 0;
    public double h = 0;

    public boolean offline = false;

    private UUID healthCheck = null;

    /**
     * Calibrates and zeroes the OTOS. Call once per OpMode init with the robot stationary:
     * calibration blocks for roughly half a second and tracking restarts from zero.
     */
    public void init(SparkFunOTOS otos) {
        this.otos = otos;

        otos.calibrateImu();
        configure();
        otos.resetTracking();

        offline = false;

        if (healthCheck != null) Scheduler.removeTask(healthCheck);
        scheduleHealthCheck();
    }

    private void configure() {
        otos.setLinearUnit(DistanceUnit.CM);
        otos.setAngularUnit(AngleUnit.RADIANS);
//        otos.setOffset(new SparkFunOTOS.Pose2D());
    }

    private void scheduleHealthCheck() {
        healthCheck = Scheduler.addTask(HEALTH_CHECK_MS, obj -> checkHealth());
    }

    /**
     * Watches for the OTOS dropping off (or coming back to) the I2C bus. This only reads the
     * product ID: it never recalibrates or resets tracking, since doing that mid-run would wipe
     * the robot's position.
     */
    private void checkHealth() {
        boolean connected;

        try {
            connected = otos.isConnected();
        } catch (Exception e) {
            connected = false;
        }

        if (!connected && !offline) {
            offline = true;
            Environment.Capabilities.otos = false;
            Logger.error("OTOS disconnected, odometry disabled");
        } else if (connected && offline) {
            try {
                // A power cycle drops the unit settings, so re-apply them. Tracking may also
                // have restarted from zero, so Autonomous stays stopped for the rest of the run.
                configure();
                offline = false;
                Environment.Capabilities.otos = true;
                Logger.warn("OTOS reconnected; its position may have reset");
            } catch (Exception e) {
                Logger.error(e);
            }
        }

        scheduleHealthCheck();
    }

    /** Clears the cached pose so a new run cannot read the previous run's values before the next tick. */
    public void reset() {
        x = 0;
        y = 0;
        h = 0;
    }

    public void tick() {
        if (otos == null || offline) return;

        SparkFunOTOS.Pose2D pos = otos.getPosition();

        x = pos.x;
        y = pos.y;
        h = pos.h;
    }
}
