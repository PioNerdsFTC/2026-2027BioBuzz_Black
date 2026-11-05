package org.pionerds.ftc.teamcode.Storage;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class StorageInitializer {
    static {
        Scheduler.addTask("init", (Obj) -> {
            if (Environment.Capabilities.launchers) {
                Globals.add(
                    "Launcher0",
                    new Launcher("Launcher0", "launcher0", "launchHood0")
                );
                Globals.add(
                    "Launcher1",
                    new Launcher("Launcher1", "launcher1", "launchHood1")
                );
            }
        });
    }
}
