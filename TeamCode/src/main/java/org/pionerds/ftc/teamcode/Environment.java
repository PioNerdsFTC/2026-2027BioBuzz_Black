package org.pionerds.ftc.teamcode;

public class Environment {
    public static class Capabilities {
        public static boolean vision = true;
        public static boolean drivetrain = false;
        public static boolean otos = false;
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
