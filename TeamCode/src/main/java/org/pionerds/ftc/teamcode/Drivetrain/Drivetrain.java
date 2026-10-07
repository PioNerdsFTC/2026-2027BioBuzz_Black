package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Driver.DriverActions;
import org.pionerds.ftc.teamcode.Driver.DriverInputs;

public class Drivetrain {
    Gamepad gamepad = new Gamepad();
    static Driver driver = new Driver();

    static {
        DriverActions action = DriverActions.LAUNCH;
        DriverInputs input = DriverInputs.MOVE_;
        driver
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input);
    }
}
