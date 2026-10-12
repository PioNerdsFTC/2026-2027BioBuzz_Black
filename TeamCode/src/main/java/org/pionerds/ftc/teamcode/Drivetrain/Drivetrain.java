package org.pionerds.ftc.teamcode.Drivetrain;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Logging.Logger;
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

            tick.set(Scheduler.addTask((obj2) -> {
                Drivetrain.omni.fieldCentricDrive();
            }));
        });

        Scheduler.addTask("exit", (obj3) -> {
            if (tick.get() != null) {
                Scheduler.removeTask(tick.get());
            }
        });
    }
}
