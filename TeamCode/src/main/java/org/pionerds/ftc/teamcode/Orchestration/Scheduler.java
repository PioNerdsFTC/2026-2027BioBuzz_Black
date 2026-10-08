package org.pionerds.ftc.teamcode.Orchestration;

import android.util.Log;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.DriverConfigurations;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Vision.Vision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;

/**
    This is the core Scheduler of the Pionerds BioBuzz Robot.
    It governs the running of events, timers, and continuous functions in a central location.

    <h2>Events</h2>
    <ul>
        <li> Prior to init, NOTHING should be done functionally and no IO should ever be called. Only basic classing is valid. </li>
        <li> TIMED events are cleared after they are called. They additionally are removed prior to a start. This is to prevent any weird race-conditions when the time resets</li>
        <li> CONTINUOUS and CONDITIONAL are not cleared as they are useful in resurrecting state after a stop (harnessing static)</li>
    </ul>

    <h2>Resurrecting state: Guidelines</h2>
    <p>
        <ul>
            <li> Use the init function to begin proper initialization: Globals and other core initialization is done prior to this event being triggered </li>
            <li> Attempt to avoid any work done outside a Scheduler init function, as static blocks will silently crash unless manually dealt with </li>
            <li> If your Subsystem requires teardown prior to a stop, you can implement this in an 'exit' event </li>
            <li> <b>Use a static block. </b> Any work done in a constructor will be recreated after a restart. As such, an 'init' event may duplicate causing mayhem</li>
        </ul>
    </p>

    <h2>Potential Issues</h2>
    <p>
        <ul>
            <li>Be wary of typing. We don't manually check type information (as Java strips types after compilation)</li>
        </ul>
    </p>
 */

public class Scheduler {

    private static final ElapsedTime time = new ElapsedTime();
    private static int iterateDepth = 0;

    public static boolean continueRunning = true;

    /**
     * Whether the Robot is properly done with init and should be fully functional
     */
    public static boolean running = true;

    /**
     * CONTINUOUS - ran each tick <br/>
     * CONDITIONAL - ran when an event is triggered <br/>
     * TIMED - ran after the designated milliseconds have occurred. <br/>
     */
    public enum ExecutionType {
        CONTINUOUS,
        CONDITIONAL,
        TIMED
    }

    public static class Task<T> {
        Consumer<T> consumer;
        ExecutionType type;
        Double timestamp;
        String event;

        public UUID id = UUID.randomUUID();

        /**
         * Create a Task which is ran every tick.
         */
        public Task(Consumer<T> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.CONTINUOUS;
        }

        /**
         * Run a Task once after <b>duration</b> ms
         */
        public Task(Integer duration, Consumer<T> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.TIMED;

            double now = time.milliseconds();
            this.timestamp = now + duration;
        }

        /**
         * Run a task after an event is called.
         */
        public Task(String event, Consumer<T> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.CONDITIONAL;
            this.event = event;
        }
    }

    private static final ArrayList<Task<?>> tasks = new ArrayList<>();
    private static final ArrayList<Task<?>> iteratingTasks = new ArrayList<>();
    private static final ArrayList<UUID> removedTasks = new ArrayList<>();

    /**
     * Add a timed task. Executes after the specified duration
     */
    public static <T> UUID addTask(Integer duration, Consumer<T> consumer) {
        Task<T> task = new Task<>(duration, consumer);

        if (iterateDepth > 0) {
            Scheduler.iteratingTasks.add(task);
            return task.id;
        }

        Scheduler.tasks.add(task);

        return task.id;
    }

    /**
     * Add a conditional task. Executes when an event trigger is called by the same event name
     */
    public static <T> UUID addTask(String event, Consumer<T> consumer) {
        Task<T> task = new Task<>(event, consumer);

        if (iterateDepth > 0) {
            Scheduler.iteratingTasks.add(task);
            return task.id;
        }
        Scheduler.tasks.add(task);

        return task.id;
    }

    /**
     * Run a task for every tick of the Scheduler
     */
    public static <T> UUID addTask(Consumer<T> consumer) {
        Task<T> task = new Task<>(consumer);

        if (iterateDepth > 0) {
            Scheduler.iteratingTasks.add(task);
            return task.id;
        }

        Scheduler.tasks.add(task);

        return task.id;
    }

    /**
     * Implementation note: on the chance of a nested removeTask in a Scheduler event, this will return true, even if a task has not yet been removed. (Which will occur once all the events have been triggered)
     * @param id
     * @return boolean
     */
    public static boolean removeTask(UUID id) {
        if (iterateDepth > 0) {
            removedTasks.add(id);
            return true;
        }

        int len = tasks.size();

        for (int i = 0; i < len; i++) {
            Task<?> current = tasks.get(i);

            if (current.id.equals(id)) {
                tasks.remove(current);
                return true;
            }
        }

        for (int i = 0; i < iteratingTasks.size(); i++) {
            Task<?> current = iteratingTasks.get(i);

            if (current.id.equals(id)) {
                iteratingTasks.remove(current);
                return true;
            }
        }

        return false;
    }

    /*
     * Triggers an <b>event</b> and passes in the selected <b>value</b> to each task.
     */
    public static <T> void trigger(String event, T val) {
        iterateDepth++;

        Iterator<Task<?>> iterator = tasks.iterator();

        try {
            while (iterator.hasNext()) {
                Task<T> task = (Task<T>) iterator.next();

                if (task.type == ExecutionType.CONDITIONAL && task.event.equals(event)) {
                    try {
                        task.consumer.accept(val);
                    } catch (Exception e) {
                        Logger.error(Log.getStackTraceString(e));
                    }
                }
            }
        } finally {
            iterateDepth--;
            if (iterateDepth == 0) flushQueues();
        }
    }

    public static void flushQueues() {
        Scheduler.tasks.addAll(Scheduler.iteratingTasks);
        Scheduler.iteratingTasks.clear();

        while (!Scheduler.removedTasks.isEmpty()) {
            removeTask(Scheduler.removedTasks.remove(0));
        }
    }

    /**
     * Triggered per-tick in the OpModes
     */
    public static void tickHook() {
        double now = time.milliseconds();

        Iterator<Task<?>> iterator = tasks.iterator();

        iterateDepth++;

        try {
            while (iterator.hasNext()) {
                Task<?> task = iterator.next();

                if (task.type == ExecutionType.CONTINUOUS) {
                    try {
                        task.consumer.accept(null);
                    } catch (Exception e) {
                        Logger.error(Log.getStackTraceString(e));
                    }
                    continue;
                }

                if (task.type == ExecutionType.TIMED && task.timestamp <= now) {
                    try {
                        task.consumer.accept(null);
                    } catch (Exception e) {
                        Logger.error(Log.getStackTraceString(e));
                    }
                    iterator.remove();
                }
            }
        } finally {
            iterateDepth--;

            if (iterateDepth == 0) flushQueues();
        }
    }

    public static void stopExecution() {
//        Scheduler.continueRunning = false;
    }

    // --- DO NOT DELETE UNLESS YOU HAVE A VERY VALID REASON --- //

    /*
        ===
        These initialize every subsystem of the robot.

        You should begin executing code in a static block
        HOWEVER, make sure to begin real init after the init event is triggered

        ===
     */

    static Logger logger = new Logger();
    static Globals globals = new Globals();
    static Hardware hardware = new Hardware();
    static Vision vision = new Vision();
    static Coordination coordination = new Coordination();
    static DriverConfigurations driverConfigurations = new DriverConfigurations();

    static {
         Scheduler.addTask("pre-init", (obj) -> {
            tasks.removeIf(task -> task.type == ExecutionType.TIMED);
            time.reset();

            Scheduler.running = false;
        });

         Scheduler.addTask("pre-run", (obj) -> {
            Scheduler.running = true;

            for (Task<?> task : tasks) {
                if (task.event == null) continue;
            }
        });

        Scheduler.addTask("exit", (obj) -> {
            Scheduler.running = false;
        });

    }
}
