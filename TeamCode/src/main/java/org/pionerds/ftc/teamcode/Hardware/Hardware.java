package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
        AtomicReference<UUID> tick = new AtomicReference<>();

        Scheduler.addTask("init", (o) -> {
            try {
                HardwareMap hardwareMap = Globals.depend("hardware-map");

                if (Environment.Capabilities.vision) {
                    webcam = hardwareMap.get(WebcamName.class, "Webcam 1");
                }

                if (Environment.Capabilities.otos) {
                    otos = hardwareMap.get(SparkFunOTOS.class, "sensor_otos");
                    Hardware.odometer.init(otos);
                }

                Hardware.mapping.init(hardwareMap);
                Hardware.gyro.init();

                List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

                for (LynxModule hub : allHubs) {
                    hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
                }

                Logger.log("Initialized Hardware");
            } catch (Exception e) {
                Logger.error(e.getMessage());
            }

            tick.set(Scheduler.addTask((obj) -> {
                Hardware.tick();
            }));
        });

        Scheduler.addTask("exit", (obj) -> {
            if (tick.get() != null) Scheduler.removeTask(tick.get());
        });
    }

    public static void tick() {
        if (!Scheduler.running) return;

        double[] angles = Hardware.gyro.getAngles();

        Hardware.yaw = angles[0];
        Hardware.pitch = angles[1];
        Hardware.roll = angles[2];

        if (Environment.Capabilities.otos) odometer.tick();
    }
}