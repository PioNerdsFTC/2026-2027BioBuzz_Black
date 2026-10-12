package org.pionerds.ftc.teamcode.Driver;

public enum DriverAction {
    ENABLE_AIMBOT,
    DISABLE_AIMBOT,
    LAUNCH,
    ENABLE_INTAKE,
    DISABLE_INTAKE,
    TURN_LEFT,
    TURN_RIGHT;

    public String toString() {
        String string = this.name();

        string = string.toLowerCase();

        int index = string.indexOf("_");

        while (string.contains("_")) {
            string = string.substring(0, index) + "-" + string.substring(index + 1);
        }

        return string;
    }
}
