package org.pionerds.ftc.teamcode.Orchestration;

import android.util.Log;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;

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

    public static final ElapsedTime time = new ElapsedTime();
    private static boolean iterating = false;

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

    public static ArrayList<Task<?>> tasks = new ArrayList<>();
    public static ArrayList<Task<?>> iteratingTasks = new ArrayList<>();
    public static ArrayList<UUID> removedTasks = new ArrayList<>();

    /**
     * Add a timed task. Executes after the specified duration
     */
    public static <T> void addTask(Integer duration, Consumer<T> consumer) {
        if (iterating) {
            Scheduler.iteratingTasks.add(new Task<>(duration, consumer));
            return;
        }
        Scheduler.tasks.add(new Task<>(duration, consumer));
    }

    /**
     * Add a conditional task. Executes when an event trigger is called by the same event name
     */
    public static <T> void addTask(String event, Consumer<T> consumer) {
        if (iterating) {
            Scheduler.iteratingTasks.add(new Task<>(event, consumer));
            return;
        }
        Scheduler.tasks.add(new Task<>(event, consumer));
    }

    /**
     * Run a task for every tick of the Scheduler
     */
    public static <T> void addTask(Consumer<T> consumer) {
        if (iterating) {
            Scheduler.iteratingTasks.add(new Task<>(consumer));
            return;
        }
        Scheduler.tasks.add(new Task<>(consumer));
    }

    /**
     * Implementation note: on the chance of a nested removeTask in a Scheduler event, this will return true, even if a task has not yet been removed. (Which will occur once all the events have been triggered)
     * @param id
     * @return boolean
     */
    public static boolean removeTask(UUID id) {
        if (iterating) {
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
        iterating = true;

        for (int i = 0; i < tasks.size(); i++) {
           Task<T> task = (Task<T>) tasks.get(i);

           if (task.type == ExecutionType.CONDITIONAL && task.event.equals(event)) {
               try {
                   task.consumer.accept(val);
               } catch(Exception e) {
                   Logger.error(Log.getStackTraceString(e));
               }
           }

           iterating = true;
        }

        iterating = false;

        flushQueues();
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

        iterating = true;

        while (iterator.hasNext()) {
            Task<?> task = iterator.next();

            if (task.type == ExecutionType.CONTINUOUS) {
                try {
                    task.consumer.accept(null);
                } catch(Exception e) {
                    Logger.error(Log.getStackTraceString(e));
                }
                continue;
            }

            if (task.type == ExecutionType.TIMED && task.timestamp <= now) {
                try {
                    task.consumer.accept(null);
                } catch(Exception e) {
                    Logger.error(Log.getStackTraceString(e));
                }
                iterator.remove();
            }

            iterating = true;
        }

        iterating = false;

        flushQueues();
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

    static {
        Scheduler.addTask("pre-init", (obj) -> {
            tasks.removeIf(task -> task.type == ExecutionType.TIMED);
            time.reset();
        });
    }
}
