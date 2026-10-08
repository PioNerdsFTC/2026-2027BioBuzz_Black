package org.pionerds.ftc.teamcode.Driver;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;

public class DriverConfigurations {
    public static Driver driver1;
    public static Driver driver2;

    static {
        Scheduler.addTask("init", (obj) -> {
            if (Parameters.operatingEnvironment != Parameters.OperatingEnvironment.TELE) {
                return;
            }
            driver1 = new Driver(Globals.depend("gamepad1"));
            driver2 = new Driver(Globals.depend("gamepad2"));

            driver1.addControl(DriverInput.A, DriverAction.LAUNCH);
        });

        UUID onLaunch = Scheduler.addTask("launch", (obj) -> {
            Logger.log("LAUNCH");
        });

        Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(onLaunch);
            driver1 = null;
            driver2 = null;
        });
    }
}
