package org.pionerds.ftc.teamcode.Autonomous;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Storage.Launcher;
import org.pionerds.ftc.teamcode.Storage.Storage;

public class AutonomousActions {

    public static boolean isLaunchingNectar;
    public static double launchingTimeNectar;
    public static boolean isLaunchingPollen;
    public static double launchingTimePollen;
    public static Launcher launcherNectar;
    public static Launcher launcherPollen;


    static{
        Scheduler.addTask("init", object -> {
            launcherNectar = Globals.depend("launcherNectar");
            launcherPollen = Globals.depend("launcherPollen");


        });
    }


    public static void launchNectar() {
        // Enable the nectar launcher
        launcherNectar.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }


    public static void launchNectar(double speed){
        // Enable the nectar launcher
        launcherNectar.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enableNectarServo();
    }

    public static void stopLaunchingNectar(){
        // Disable the nectar launcher
        launcherNectar.disableLauncher();

        // Disable the feeding servo for the nectar launcher
        Storage.disableNectarServo();
    }

    public static void launchPollen() {
        // Enable the nectar launcher
        launcherPollen.enableLauncher();

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }

    public static void launchPollen(double speed){
        // Enable the nectar launcher
        launcherPollen.enableLauncher(speed);

        // Enable the feeding servo for the nectar launcher
        Storage.enablePollenServo();
    }


    public static void stopLaunchingPollen(){
        // Disable the nectar launcher
        launcherPollen.disableLauncher();

        // Disable the feeding servo for the nectar launcher
        Storage.disablePollenServo();
    }


}
