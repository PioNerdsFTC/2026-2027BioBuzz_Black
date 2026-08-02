package org.pionerds.ftc.teamcode.Orchestration;

import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
    Seems to be popular these days. This is just an attempt at a state machine.
 */
public class Scheduler {

    private static final ElapsedTime time = new ElapsedTime();

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

        /**
         * Create a Task which is ran every tick.
         */
        public Task(Consumer<T> consumer) {
            this.consumer = consumer;
            this.type = ExecutionType.CONTINUOUS;
        }

        /**
         * Run a Task once after <b>duration</b> ms, currently seems to be broken
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

    public static ArrayList<Task> tasks = new ArrayList<>();

    /**
     * Add a task into the task queue.
     */
    public static void addTask(Integer duration, Consumer consumer) {
        Scheduler.tasks.add(new Task(duration, consumer));
    }

    public static void addTask(String event, Consumer consumer) {
        Scheduler.tasks.add(new Task(event, consumer));
    }

    public static void addTask(Consumer consumer) {
        Scheduler.tasks.add(new Task(consumer));
    }

    /*
     * Triggers an <b>event</b> and passes in the selected <b>object</b> to each task.
     */
    public static void trigger(String event, Object object) {
        Task task;
        for (int i = 0; i < tasks.size(); i++) {
            task = tasks.get(i);

            if (task.type == ExecutionType.CONDITIONAL && task.event.equals(event)) {
                task.consumer.accept(object);

                tasks.remove(task);
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

        for (Task task : tasks) {
            if (task.type == ExecutionType.CONTINUOUS) {
                task.consumer.accept(null);
                continue;
            }

            if (task.type == ExecutionType.TIMED && task.timestamp <= now) {
                task.consumer.accept(null);
                tasks.remove(task);
            }
        };
    }
}
