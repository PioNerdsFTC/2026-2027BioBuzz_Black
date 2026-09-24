
package org.pionerds.ftc.teamcode.Logging;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;

/**
 * Goal of this is to make a unified interface for writing errors and logs cleanly.
 * Right now we really don't have a solution for proper logs outside of actually writing to the screen.
 * Eventually we should be able to configure different levels of reporting (and maybe some way to export logs?)
 */
public class Logger {

    private Telemetry telemetry;

    public Logger() {
        Scheduler.addTask("init", (obj) -> {
            telemetry = Globals.depend("telemetry");
            telemetry.setAutoClear(false);

            telemetry.addLine("Starting...");
            telemetry.update();

            Scheduler.addTask(5000, (obj2) -> {
                telemetry.addLine("hello from 5 second in the future");
                telemetry.update();
            });

            Scheduler.addTask("log:new:info", (info) -> {
                logs.add((String) info);
                level.add(LogType.INFO);

                telemetry.update();
            });

            Scheduler.addTask("log:new:warn", (info) -> {
                logs.add((String) info);
                level.add(LogType.WARNING);

                telemetry.update();
            });

            Scheduler.addTask("log:new:error", (info) -> {
                logs.add((String) info);
                level.add(LogType.ERROR);

                telemetry.update();
            });
        });

        Scheduler.addTask("exit", (obj) -> {
            telemetry.clear();
        });

    }

    public enum LogType {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    // Unless we have a way to get a tuple type, this is *a* solution.
    // Each index should correspond to the same log in both Arraylists.
    public ArrayList<String> logs = new ArrayList<>();
    public ArrayList<LogType> level = new ArrayList<>();

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
}