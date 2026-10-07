package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Driver.Driver;

public class Drivetrain {
    Gamepad gamepad = new Gamepad();
    Driver driver = new Driver(gamepad);

    driver
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input)
            .addControl(action, input);

}
