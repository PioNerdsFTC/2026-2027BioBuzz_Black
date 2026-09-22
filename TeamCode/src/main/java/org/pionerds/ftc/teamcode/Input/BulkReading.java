package org.pionerds.ftc.teamcode.Input;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.List;

public class BulkReading {
    private static HardwareMap hardwareMap;

    BulkReading() {
        BulkReading.hardwareMap = Globals.depend("hardware-map");

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Scheduler.addTask((obj) -> {
        });
    }
}
