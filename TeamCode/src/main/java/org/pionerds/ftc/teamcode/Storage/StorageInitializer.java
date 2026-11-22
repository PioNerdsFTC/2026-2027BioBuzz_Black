package org.pionerds.ftc.teamcode.Storage;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class StorageInitializer {

    static {
        Scheduler.addTask("init", Obj -> {
            if (Environment.Capabilities.launchers) {
                Globals.add(
                    "launcherNectar",
                    new Launcher(
                        "launcherNectar",
                        "bigLauncher",
                        "bigHoodAdjust",
                        false
                    )
                );

                Launcher launcherPollen = new Launcher(
                    "launcherPollen",
                    "littleLauncher",
                    "littleHoodAdjust",
                    true
                );
                Globals.add("launcherPollen", launcherPollen);
                launcherPollen.registerVelocityControls(
                    "increase-pollen-speed:down",
                    "decrease-pollen-speed:down"
                );
            }
        });
    }
}
