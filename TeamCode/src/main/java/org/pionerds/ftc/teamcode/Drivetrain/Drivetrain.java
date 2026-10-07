package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Driver.DriverActions;
import org.pionerds.ftc.teamcode.Driver.DriverInputs;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Drivetrain {
    Gamepad gamepad = new Gamepad();
    static Driver driver;

    static {
        Scheduler.addTask("init", (obj) -> {
            DriverActions action = DriverActions.LAUNCH;
            DriverInputs input = DriverInputs.MOVE_;

            driver = new Driver(Globals.depend());

            driver
                    .addControl(action, input)
                    .addControl(action, input)
                    .addControl(action, input)
                    .addControl(action, input)
                    .addControl(action, input)
                    .addControl(action, input);
        });
    }
}
