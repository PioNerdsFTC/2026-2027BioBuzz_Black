package org.pionerds.ftc.teamcode.Driver;

public enum DriverInput {
    LEFT_STICK_Y,
    LEFT_STICK_X,
    RIGHT_STICK_Y,
    RIGHT_STICK_X,
    A,
    B,
    X,
    Y,
    RIGHT_TRIGGER,
    LEFT_TRIGGER,
    RIGHT_BUMPER,
    LEFT_BUMPER,
    LEFT_STICK_BUTTON,
    RIGHT_STICK_BUTTON,
    DPAD_UP,
    DPAD_DOWN,
    DPAD_RIGHT,
    DPAD_LEFT;

    public boolean isToggle = false;

    public void setToggle() {
        this.isToggle = true;
    }
}
