package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.List;

/**
 * Manages reading & writing for servos, motors and sensors.
 */
public final class Hardware {
    public static Mapping mapping = new Mapping();
    public static Gyro gyro = new Gyro();
    public static Odometer odometer = new Odometer();
    public static WebcamName webcam;
    public static SparkFunOTOS otos;

    public static double yaw = 0;
    public static double pitch = 0;
    public static double roll = 0;

    static {
        Scheduler.addTask((obj) -> Hardware.tick());

        Scheduler.addTask("init", (o) -> {
            try {
                HardwareMap hardwareMap = Globals.depend("hardware-map");

                Hardware.mapping.init(hardwareMap);

                if (Environment.Capabilities.vision) {
                    webcam = hardwareMap.get(WebcamName.class, "Webcam 1");
                }

                if (Environment.Capabilities.otos) {
                    try {
                        otos = hardwareMap.get(SparkFunOTOS.class, "otos");
                        Hardware.odometer.init(otos);
                    } catch (Exception e) {
                        // A missing OTOS must not take the rest of init (gyro, bulk caching) down
                        otos = null;
                        Environment.Capabilities.otos = false;
                        Logger.warn("OTOS not available, odometry disabled");
                    }
                }

                Hardware.gyro.init();

                // Drop the previous run's cached angles: the gyro was just reset, and the
                // continuous tick has not refreshed these fields yet.
                Hardware.yaw = 0;
                Hardware.pitch = 0;
                Hardware.roll = 0;

                // Same for the odometer, whose tracking was reset above
                Hardware.odometer.reset();

                List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

                for (LynxModule hub : allHubs) {
                    hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
                }

                Logger.log("Initialized Hardware");
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    public static void tick() {
        if (!Scheduler.running) return;

        double[] angles = Hardware.gyro.getAngles();

        // The IMU reports yaw in [-pi, pi]; expose it as [0, 2pi) so the wrap happens
        // in one place and every consumer sees the same range.
        Hardware.yaw = normalizeYaw(angles[0]);
        Hardware.pitch = angles[1];
        Hardware.roll = angles[2];

        if (Environment.Capabilities.otos && otos != null) odometer.tick();
    }

    private static double normalizeYaw(double radians) {
        double twoPi = 2 * Math.PI;
        double angle = radians % twoPi;

        return angle < 0 ? angle + twoPi : angle;
    }
}
