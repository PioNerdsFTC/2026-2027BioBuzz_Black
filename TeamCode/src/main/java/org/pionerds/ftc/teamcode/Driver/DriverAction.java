package org.pionerds.ftc.teamcode.Driver;

public enum DriverAction {
    ENABLE_AIMBOT,
    DISABLE_AIMBOT,
    TOGGLE_INTAKE,
    TRIGGER_FLOWER_LAUNCHER,
    TRIGGER_POLLEN_LAUNCHER,
    TOGGLE_LAUNCHER,
    INCREASE_POLLEN_SPEED,
    DECREASE_POLLEN_SPEED,
    TURN_LEFT,
    TRIGGER_INTAKE,
    TRIGGER_EJECTION_INTAKE,
    POLLEN_UP,
    POLLEN_DOWN,
    LID_DOWN,
    LID_UP,
    TURN_RIGHT;

    public String toString() {
        return this.name().toLowerCase().replace('_', '-');
    }
}
