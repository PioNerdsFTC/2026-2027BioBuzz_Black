
package org.pionerds.ftc.teamcode.Logging;

import java.util.ArrayList;

/**
 * Goal of this is to make a unified interface for writing errors and logs cleanly.
 * Right now we really don't have a solution for proper logs outside of actually writing to the screen.
 * Eventually we should be able to configure different levels of reporting (and maybe some way to export logs?)
 */
public class Logger {

    public static enum LogType {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    // Unless we have a way to get a tuple type, this is *a* solution.
    // Each index should correspond to the same log in both Arraylists.
    public static ArrayList<String> logs = new ArrayList<String>();
    public static ArrayList<LogType> level = new ArrayList<LogType>();

    /**
     * Log a debug message
     */
    public static void debug(LogType type, String log) {
        Logger.logs.add(log);
        Logger.level.add(LogType.DEBUG);
    }

    /**
     * Log a simple log message
     */
    public static void log(String log) {
        Logger.logs.add(log);
        Logger.level.add(LogType.INFO);
    }

    /**
     * Log a warning message
     */
    public static void warn(String log) {
        Logger.logs.add(log);
        Logger.level.add(LogType.WARNING);
    }

    /**
     * Log an error
     */
    public static void error(String log) {
        Logger.logs.add(log);
        Logger.level.add(LogType.ERROR);
    }
}