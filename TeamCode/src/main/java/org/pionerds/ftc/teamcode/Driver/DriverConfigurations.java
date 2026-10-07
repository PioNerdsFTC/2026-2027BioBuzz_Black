package org.pionerds.ftc.teamcode.Driver;

import org.pionerds.ftc.teamcode.Orchestration.Globals;

public class DriverConfigurations {
    public static Driver driver1;
    public static Driver driver2;

    static {
        driver1 = new Driver(Globals.depend("Gamepad1"));
        driver2 = new Driver(Globals.depend("Gamepad2"));

        driver1 .addControl()
                .addControl()
                .addControl()
                .addControl()
                .addControl();


        driver2 .addControl()
                .addControl()
                .addControl()
                .addControl()
                .addControl();


    }
}
