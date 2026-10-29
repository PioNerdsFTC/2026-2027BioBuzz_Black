
package org.pionerds.ftc.teamcode.Logging;

import android.util.Log;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unified Logging method. Reports to the Dashboard
 */
public class Logger {

    private static final String TAG = "PioNerds-Runtime";
    private static Telemetry telemetry;

    public enum LogType {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    // Unless we have a way to get a tuple type, this is *a* solution.
    // Each index should correspond to the same log in both Arraylists.
    public static ArrayList<String> logs = new ArrayList<>();
    public static ArrayList<LogType> level = new ArrayList<>();

    /**
     * Log a debug message
     */
    public void debug(LogType type, String log) {
        logs.add(log);
        level.add(LogType.DEBUG);
    }

    /**
     * Log a simple log message
     */
    public static void log(String log) {
        Scheduler.trigger("log:new:info", log);
    }

    /**
     * Log a warning message
     */
    public static void warn(String log) {
        Scheduler.trigger("log:new:warn", log);
    }

    /**
     * Log an error
     */
    public static void error(String log) {
        Scheduler.trigger("log:new:error", log);
    }

    public static void error(Exception e) {
        Scheduler.trigger("log:new:error", Log.getStackTraceString(e));
    }

    public static void clear() {
        Scheduler.trigger("log:clear", null);
    }

    static {
        Logger.logs.clear();
        Logger.level.clear();

        Scheduler.addTask("init", (obj2) -> {
            telemetry = Globals.depend("telemetry");
            telemetry.setAutoClear(false);
        });

        Scheduler.addTask("exit", (obj3) -> {
            telemetry = null;
        });

        Scheduler.addTask("log:new:info", (info) -> {
            Logger.logs.add((String) info);
            Logger.level.add(LogType.INFO);

            Log.i(TAG, (String) info);

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:new:warn", (info) -> {
            logs.add((String) info);
            level.add(LogType.WARNING);

            Log.w(TAG, (String) info);

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:new:error", (info) -> {
            logs.add((String) info);
            level.add(LogType.ERROR);

            if (Environment.Parameters.exitOnError) {
                Scheduler.stopExecution();

                Logger.log("STOPPING EXECUTION DUE TO ERROR");
            }

            Log.e(TAG, (String) info);

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:clear", (info) -> {
            while (!logs.isEmpty()) logs.remove(0);

            if (telemetry == null) return;

            telemetry.clear();
            telemetry.update();
        });
    }
}