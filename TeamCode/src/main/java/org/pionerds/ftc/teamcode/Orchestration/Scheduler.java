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
    Seems to be popular these days. This is just an attempt at a state machine.

    EVENTS:
        - prior to init, NOTHING should be done functionally and no IO should ever be called. Only basic classing is valid.
 */
public class Scheduler {

    public static final ElapsedTime time = new ElapsedTime();

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
        Consumer<Object> consumer;
        ExecutionType type;
        Double timestamp;
        String event;

        public UUID id = UUID.randomUUID();

        /**
         * Create a Task which is ran every tick.
         */
        public Task(Consumer<Object> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.CONTINUOUS;
        }

        /**
         * Run a Task once after <b>duration</b> ms, currently seems to be broken
         */
        public Task(Integer duration, Consumer<Object> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.TIMED;

            double now = time.milliseconds();
            this.timestamp = now + duration;
        }

        /**
         * Run a task after an event is called.
         */
        public Task(String event, Consumer<Object> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.CONDITIONAL;
            this.event = event;
        }
    }

    public static ArrayList<Task<Object>> tasks = new ArrayList<>();

    /**
     * Add a task into the task queue.
     */
    public static void addTask(Integer duration, Consumer<Object> consumer) {
        Scheduler.tasks.add(new Task<Object>(duration, consumer));
    }

    public static void addTask(String event, Consumer<Object> consumer) {
        Scheduler.tasks.add(new Task<Object>(event, consumer));
    }

    /**
     * Run a task for every tick of the Scheduler
     * @param consumer lambda to run
     */
    public static void addTask(Consumer<Object> consumer) {
        Scheduler.tasks.add(new Task<Object>(consumer));
    }

    public static void removeTask(UUID id) {
        int len = tasks.size();

        for (int i = 0; i < len; i++) {
            Task<Object> current = tasks.get(i);

            if (current.id.equals(id)) {
                tasks.remove(current);
                return;
            }
        }
    }

    /*
     * Triggers an <b>event</b> and passes in the selected <b>object</b> to each task.
     */
    public static void trigger(String event, Object object) {
        for (int i = 0; i < tasks.size(); i++) {
            Task<Object> task = tasks.get(i);

            if (task.type == ExecutionType.CONDITIONAL && task.event.equals(event)) {
                try {
                    task.consumer.accept(object);
                } catch(Exception e) {
                    Logger.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Runs per-tick
     */
    public static void tickHook() {
        // Calculating the time now, instead of in the loop.
        // Not sure whether this is necessary or counterproductive.

        double now = time.milliseconds();

        Iterator<Task<Object>> iterator = tasks.iterator();

        while (iterator.hasNext()) {
            Task<Object> task = iterator.next();

            if (task.type == ExecutionType.CONTINUOUS) {
                try {
                    task.consumer.accept(null);
                } catch(Exception e) {
                    Logger.error(e.getMessage());
                }
                continue;
            }

            if (task.type == ExecutionType.TIMED && task.timestamp <= now) {
                try {
                    task.consumer.accept(null);
                } catch(Exception e) {
                    Logger.error(e.getMessage());
                }
                iterator.remove();
            }
        };
    }

    static Logger logger = new Logger();
    static Globals globals = new Globals();
    static Hardware hardware = new Hardware();

    static {
        Scheduler.addTask("pre-init", (obj) -> {
            time.reset();
        });
    }
}
