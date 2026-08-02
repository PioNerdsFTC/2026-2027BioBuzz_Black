package org.pionerds.ftc.teamcode.Scheduler;

import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
    Seems to be popular these days. This is just an attempt at a state machine.
 */
public class Scheduler {

    private static final ElapsedTime runtime = new ElapsedTime();

    /**
     * CONTINUOUS - ran each tick <br/>
     * CONDITIONAL - ran when an event is triggered <br/>
     * TIMED - ran after the designated milliseconds have occurred. <br/>
     */
    public static enum ExecutionType {
        CONTINUOUS,
        CONDITIONAL,
        TIMED
    }

    //  For now (and testing purposes), only CONTINUOUS is going to be ran
    public static class Task {
        Runnable runnable;
        ExecutionType type;
        Long timestamp;
        String event;

        /**
         * Create a Task which is ran every tick.
         */
        Task(Runnable runnable) {
            this.runnable = runnable;
            this.type = ExecutionType.CONTINUOUS;
        }

        /**
         * Run a Task once after <b>duration</b> ms
         */
        public Task(Runnable runnable, Integer duration) {
            this.runnable = runnable;
            this.type = ExecutionType.TIMED;

            long now = runtime.now(TimeUnit.MILLISECONDS);
            this.timestamp = now + (long) duration;
        }

        /**
         * Run a task after an event is called.
         */
        Task(Runnable runnable, String event) {
            this.runnable = runnable;
            this.type = ExecutionType.CONDITIONAL;
            this.event = event;
        }

        public void lambda(String s) {
            runnable.run();
        }
    }

    public static ArrayList<Task> tasks = new ArrayList<>();

    public static void addTask(Task task) {
        Scheduler.tasks.add(task);
    }

    /*
     *
     */
    public static void trigger(String event) {
        Task task;
        for (int i = 0; i < tasks.size(); i++) {
            task = tasks.get(i);

            if (task.type == ExecutionType.CONDITIONAL && task.event.equals(event)) {
                task.runnable.run();

                tasks.remove(task);
            }
        }
    }

    /**
     * Runs per-tick
     */
    public static void tickHook() {
        Task task;

        // Calculating the time now, instead of in the loop.
        // Not sure whether this is necessary or counterproductive.
        long now = runtime.now(TimeUnit.MILLISECONDS);

        for (int i = 0; i < tasks.size(); i++) {
            task = tasks.get(i);

            if (task.type == ExecutionType.CONTINUOUS) {
                task.runnable.run();
            }

            if (task.type == ExecutionType.TIMED && task.timestamp >= now) {
                task.runnable.run();

                tasks.remove(task);
            }
        }
    }
}
