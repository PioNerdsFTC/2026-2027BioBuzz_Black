package org.pionerds.ftc.teamcode;

public class Environment {
    public static class Capabilities {
        public static boolean vision = false;
        public static boolean drivetrain = true;
        public static boolean otos = false;
        public static boolean launchers = false;
    }

    public static class Parameters {
        public enum OperatingEnvironment {
            AUTO,
            TELE,

            NONE
        };
        public static OperatingEnvironment operatingEnvironment = OperatingEnvironment.NONE;
        public static boolean exitOnError = true; // Should be set to false in actual operation
        public static boolean competing = false;
    }
}
