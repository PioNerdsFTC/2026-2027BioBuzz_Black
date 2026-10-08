
package org.pionerds.ftc.teamcode.Logging;

import android.util.Log;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unified Logging method. Reports to the Dashboard
 */
public class Logger {

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
        AtomicReference<UUID> logNewInfo = new AtomicReference<>();
        AtomicReference<UUID> logNewWarn = new AtomicReference<>();
        AtomicReference<UUID> logNewError = new AtomicReference<>();
        AtomicReference<UUID> logClear = new AtomicReference<>();

        Scheduler.addTask("init", (obj2) -> {
            telemetry = Globals.depend("telemetry");
            telemetry.setAutoClear(false);
        });

        Scheduler.addTask("pre-init", (obj) -> {

            logNewInfo.set(Scheduler.addTask("log:new:info", (info) -> {
                Logger.logs.add((String) info);
                Logger.level.add(LogType.INFO);

                Log.i("PioNerds-runtime", (String) info);

                if (telemetry == null) return;

                telemetry.addLine((String) info);
                telemetry.update();
            }));

            logNewWarn.set(Scheduler.addTask("log:new:warn", (info) -> {
                logs.add((String) info);
                level.add(LogType.WARNING);

                Log.w("PioNerds-runtime", (String) info);

                if (telemetry == null) return;

                telemetry.addLine((String) info);
                telemetry.update();
            }));

            logNewError.set(Scheduler.addTask("log:new:error", (info) -> {
                logs.add((String) info);
                level.add(LogType.ERROR);

                if (Parameters.exitOnError) {
                    Scheduler.stopExecution();

                    Logger.log("STOPPING EXECUTION DUE TO ERROR");
                }

                Log.e("PioNerds-runtime (Error)", (String) info);


                if (telemetry == null) return;

                telemetry.addLine((String) info);
                telemetry.update();
            }));

            logClear.set(Scheduler.addTask("log:clear", (info) -> {
                while (!logs.isEmpty()) logs.remove(0);

                if (telemetry == null) return;

                telemetry.clear();
                telemetry.update();
            }));
        });

        Scheduler.addTask("exit", (obj) -> {
            telemetry.clear();

            if (logNewInfo.get() != null) Scheduler.removeTask(logNewInfo.get());
            if (logNewWarn.get() != null) Scheduler.removeTask(logNewWarn.get());
            if (logNewError.get() != null) Scheduler.removeTask(logNewError.get());
        });
    }
}