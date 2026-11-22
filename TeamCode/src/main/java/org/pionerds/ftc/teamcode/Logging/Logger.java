package org.pionerds.ftc.teamcode.Logging;

import android.util.Log;

import com.qualcomm.robotcore.util.ReadWriteFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

/**
 * Unified Logging method. Reports to the Dashboard
 */
public class Logger {

    private static final String TAG = "PioNerds-Runtime";
    private static Telemetry telemetry;


    private static SimpleDateFormat formatter;
    private static String fileName;
    private static File file;

    private static void saveLogs() {
        String formattedLogs = "";

        for (int i = 0; i < logs.size(); i++) {
            LogType currentLevel = level.get(i);
            formattedLogs += formatter.format(new Date()) + " " + currentLevel.toString().toUpperCase() + ": " + logs.get(i) + "\n";
        }

        ReadWriteFile.writeFile(file, formattedLogs);

        Scheduler.addTask(1000, obj -> saveLogs());
    }

    static {
        UUID preInit = Scheduler.addTask("pre-init", obj -> {
            formatter = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss", Locale.US);
            fileName = formatter.format(new Date()) + "-" + Environment.Parameters.operatingEnvironment.toString().toLowerCase();
            file  = AppUtil.getInstance().getSettingsFile(fileName);
        });

        Scheduler.addTask("init", obj -> {
            if (!Environment.Parameters.competing) return;

            Scheduler.addTask(1000, obj2 -> {
                saveLogs();
            });
        });
    }

    public enum LogType {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
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

        Scheduler.addTask("init", obj2 -> {
            telemetry = Globals.depend("telemetry");
            telemetry.setAutoClear(false);
        });

        Scheduler.addTask("exit", obj3 -> {
            telemetry = null;
        });

        Scheduler.addTask("log:new:info", info -> {
            Logger.logs.add((String) info);
            Logger.level.add(LogType.INFO);

            Log.i(TAG, String.valueOf(info));

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:new:warn", info -> {
            logs.add((String) info);
            level.add(LogType.WARNING);

            Log.w(TAG, String.valueOf(info));

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:new:error", info -> {
            logs.add((String) info);
            level.add(LogType.ERROR);

            if (Environment.Parameters.exitOnError) {
                Scheduler.stopExecution();

                Logger.log("STOPPING EXECUTION DUE TO ERROR");
            }

            Log.e(TAG, String.valueOf(info));

            if (telemetry == null) return;

            telemetry.addLine((String) info);
            telemetry.update();
        });

        Scheduler.addTask("log:clear", info -> {
            logs.clear();
            level.clear();

            if (telemetry == null) return;

            telemetry.clear();
            telemetry.update();
        });
    }
}
