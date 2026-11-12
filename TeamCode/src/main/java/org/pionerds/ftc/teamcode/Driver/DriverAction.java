package org.pionerds.ftc.teamcode.Driver;

public enum DriverAction {
    ENABLE_AIMBOT,
    DISABLE_AIMBOT,
    TOGGLE_INTAKE,
    TRIGGER_FLOWER_LAUNCHER,
    TRIGGER_POLLEN_LAUNCHER,
    TOGGLE_LAUNCHER,
    TURN_LEFT,
    TRIGGER_INTAKE,
    TRIGGER_EJECTION_INTAKE,
    TURN_RIGHT;


    public String toString() {
        return this.name().toLowerCase().replace('_', '-');
    }
}
