package org.pionerds.ftc.teamcode.Orchestration;

import android.graphics.Path;

public class Parameters {
    public enum OperatingEnvironment {
        AUTO,
        TELE,

        NONE
    };
    public static OperatingEnvironment operatingEnvironment = OperatingEnvironment.NONE;
    public static boolean exitOnError = true; // Should be set to false in actual operation
    public static boolean competing = false;
}
