package org.pionerds.ftc.teamcode;

public class Environment {

    public static class Capabilities {

        public static boolean vision = true;
        public static boolean drivetrain = true;
        public static boolean useFieldCentricDriving = true;
        public static boolean otos = true;
        public static boolean launchers = true;
        public static boolean aimbotEnabled = true;
        public static boolean isOppositeDriveTele = true;
    }

    public static class Parameters {

        public enum OperatingEnvironment {
            AUTO,
            TELE,

            NONE,
        }

        public enum Side {
            BLUE,
            RED,
        }

        public static OperatingEnvironment operatingEnvironment =
            OperatingEnvironment.NONE;
        public static boolean exitOnError = false; // Should be set to false in actual operation
        public static boolean competing = false;

        // Runtime parameters

        public static Side alliance = Side.BLUE;



        // Disables normal auto, enabled in the AutoBackup class.
        public static boolean emergencyAuto = false;
    }
}
