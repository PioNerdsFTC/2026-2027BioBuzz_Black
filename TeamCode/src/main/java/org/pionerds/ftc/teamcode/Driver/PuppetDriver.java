package org.pionerds.ftc.teamcode.Driver;

import com.qualcomm.robotcore.hardware.Gamepad;

public class PuppetDriver extends Driver {

    double x = 0.00;
    double y = 0.00;


    public PuppetDriver() {
        super(null);
    }

    @Override
    public double leftStickX() {
        return x;
    }

    @Override
    public double leftStickY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
