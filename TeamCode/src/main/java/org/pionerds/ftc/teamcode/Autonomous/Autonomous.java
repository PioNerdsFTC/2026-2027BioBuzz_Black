package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Action;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Step;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.OmniDrivetrain;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Storage.Intake;
import org.pionerds.ftc.teamcode.Storage.Launcher;
import org.pionerds.ftc.teamcode.Storage.Storage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

public class Autonomous {

    private static double xf = 0.00;
    private static double yf = 0.00;
    private static double x = 0.00;
    private static double y = 0.00;
    private static double t = 0.00;

    private static double maxSpeed = 0.50;
    private static double mediumSpeed = 0.20;
    private static double slowSpeed = 0.05;
    private static PuppetDriver puppet = new PuppetDriver();

    private static final ArrayList<Step> stepQueue = new ArrayList<>();
    private static final ArrayDeque<Action> actionQueue = new ArrayDeque<>();
    private static final ElapsedTime actionTimer = new ElapsedTime();

    private static Action currentAction = null;
    private static double currentActionEndsAt = 0;

    /**
     * Program uploaded from the dashboard. Loaded into the queues when an autonomous run starts.
     * Replaced wholesale (never mutated) since it is written from the web server thread.
     */
    private static volatile AutonomousProgram program = AutonomousProgram.EMPTY;


    static{
        OmniDrivetrain.OVERRIDE_DRIVER(puppet);

        Scheduler.addTask((obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.AUTO) return;
            if (!Scheduler.running) return;

            // Actions run with the robot stationary, before moving on to the next waypoint
            if (currentAction != null || !actionQueue.isEmpty()) {
                stop();
                tickActions();
                return;
            }

            if (stepQueue.isEmpty()) {
                stop();
                return;
            }

            moveToPositionTick(stepQueue.get(0));
        });

        // Runs after Coordination's own init (it is registered earlier), so this overrides its default start
        Scheduler.addTask("init", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.AUTO) return;

            double[] start = program.start;
            if (start != null) Coordination.setStart(start[0], start[1]);
        });

        Scheduler.addTask("pre-run", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.AUTO) return;

            AutonomousProgram current = program;

            clearQueues();
            actionQueue.addAll(current.startActions);
            stepQueue.addAll(current.steps);
        });

        Scheduler.addTask("exit", (obj) -> {
            clearQueues();
            stop();
        });
    }


    /**
     * Queue a movement to {x, y, tolerance} in Coordination units
     */
    public static void requestMovement(double[] xyT){
        stepQueue.add(new Step(xyT[0], xyT[1], xyT[2], Collections.emptyList()));
    }

    public static void setProgram(AutonomousProgram program) {
        Autonomous.program = program;
    }

    public static AutonomousProgram getProgram() {
        return program;
    }

    public static void moveToPositionTick(Step step){
        cacheTarget(step);

        if(isWithinTolerance()){
            stepQueue.remove(step);
            actionQueue.addAll(step.actions);
            stop();
            return;
        }

        puppet.setX(calculateSpeedForceX());
        puppet.setY(-calculateSpeedForceY());
    }

    private static void tickActions() {
        if (currentAction != null) {
            if (actionTimer.milliseconds() < currentActionEndsAt) return;

            finishAction(currentAction);
            currentAction = null;
        }

        // Instant actions all run in the same tick, timed ones hold the queue until they finish
        while (!actionQueue.isEmpty()) {
            Action action = actionQueue.poll();
            double duration = startAction(action);

            if (duration > 0) {
                currentAction = action;
                currentActionEndsAt = actionTimer.milliseconds() + duration;
                return;
            }

            finishAction(action);
        }
    }

    /**
     * @return how long the action takes in ms, 0 if it completes immediately
     */
    private static double startAction(Action action) {
        switch (action.type) {
            case INTAKE:
                Logger.log("Autonomous: intake " + action.mode);

                if (action.mode.equals("in")) {
                    Intake.setPower(1);
                    Storage.enableIntake();
                } else if (action.mode.equals("out")) {
                    Intake.setPower(-1);
                    Storage.disableIntake();
                } else {
                    Intake.setPower(0);
                    Storage.disableIntake();
                }
                return 0;

            case LAUNCHER:
                Logger.log("Autonomous: launcher " + (action.enabled ? "on" : "off") + " at " + action.value + " deg");

                for (String name : new String[] { "Launcher0", "Launcher1" }) {
                    if (!Globals.has(name)) continue;

                    Launcher launcher = Globals.depend(name);
                    launcher.setLaunchAngle(action.value);

                    if (action.enabled) launcher.enableLauncher();
                    else launcher.disableLauncher();
                }
                return 0;

            case FEED:
                Logger.log("Autonomous: feeding " + action.mode + " for " + action.value + " ms");

                if (action.mode.equals("pollen")) Storage.enablePollenServo();
                else Storage.enableNectarServo();
                return action.value;

            case WAIT:
                return action.value;
        }

        return 0;
    }

    private static void finishAction(Action action) {
        if (action.type != Action.Type.FEED) return;

        if (action.mode.equals("pollen")) Storage.disablePollenServo();
        else Storage.disableNectarServo();
    }

    private static void clearQueues() {
        if (currentAction != null) finishAction(currentAction);

        currentAction = null;
        actionQueue.clear();
        stepQueue.clear();
    }

    private static void stop(){
        puppet.setX(0.00);
        puppet.setY(0.00);
    }


    private static void cacheTarget(Step target){
        x = Coordination.x;
        y = Coordination.y;

        xf = target.x;
        yf = target.y;
        t = target.tolerance;
    }

    private static boolean isWithinTolerance(){
        if(Math.abs(xf-x) <= t && Math.abs(yf-y) <= t) return true;
        return false;
    }

    private static double getΔ(double p2, double p1){
        return Math.abs(p2-p1);
    }

    private static double calculateSpeedForceX(){
        return calculateSpeedForce(xf, x);
    }

    private static double calculateSpeedForceY(){
        return calculateSpeedForce(yf, y);
    }

    private static double calculateSpeedForce(double target, double current){
        double Δ = getΔ(target, current);
        double direction = Math.signum(target - current);

        if(Δ <= t) return 0.00;
        else if(Δ >= 20) return direction * maxSpeed;
        else if(Δ >= 10) return direction * mediumSpeed;
        else return direction * slowSpeed;
    }
}
