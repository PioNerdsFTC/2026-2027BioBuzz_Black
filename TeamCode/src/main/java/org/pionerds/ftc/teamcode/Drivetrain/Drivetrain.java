package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Driver.DriverAction;
import org.pionerds.ftc.teamcode.Driver.DriverInput;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Drivetrain {
    static Driver driver;

    static {
        Scheduler.addTask("init", (obj) -> {
            Logger.log("Initializing drivetrain");
        });
    }

    private boolean permitDriving = true;

    // INSERT METHOD HERE FOR CALLING OmniDrive.java.fieldCentricDrive();

    // INSERT METHOD HERE FOR CALLING OmniDrive.java.rotateWithBumpers();

}
