package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Driver.DriverAction;
import org.pionerds.ftc.teamcode.Driver.DriverInput;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Drivetrain {
    Gamepad gamepad = new Gamepad();
    static Driver driver;

    static {
        Scheduler.addTask("init", (obj) -> {
            Logger.log("Initializing drivetrain");
        });
    }
}
