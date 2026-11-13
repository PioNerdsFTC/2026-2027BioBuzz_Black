package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Action;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Step;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.Drivetrain;
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
import java.util.List;

public class Autonomous {

    private static double xf = 0.00;
    private static double yf = 0.00;
    private static double x = 0.00;
    private static double y = 0.00;
    private static double t = 0.00;

    private static double r = 0.00;
    private static Double rf = null;
    private static Double lastLoggedRotation = null;

    /** How close the robot has to be to a waypoint's heading, in radians. */
    private static final double ROTATION_TOLERANCE = Math.toRadians(2);

    private static double maxSpeed = 0.50;
    private static double mediumSpeed = 0.20;
    private static double slowSpeed = 0.05;
    private static PuppetDriver puppet = new PuppetDriver();

    /** How long the launcher needs to reach speed before the first shot. */
    private static final double LAUNCHER_SPIN_UP_MS = 1000;
    /** How long a feed servo runs to push one piece into the launcher. */
    private static final double FEED_MS = 400;
    /** Pause between shots so the launcher recovers speed. */
    private static final double FEED_GAP_MS = 300;

    /** Hardware commands that actions are broken down into. */
    private static final class Op {
        enum Kind { LAUNCHER_ON, LAUNCHER_OFF, FEED_ON, FEED_OFF, INTAKE_ON, INTAKE_OFF, DELAY }

        final Kind kind;
        final String piece;
        final double value;

        Op(Kind kind, String piece, double value) {
            this.kind = kind;
            this.piece = piece;
            this.value = value;
        }

        Op(Kind kind) { this(kind, null, 0); }
    }

    private static final ArrayList<Step> stepQueue = new ArrayList<>();
    private static final ArrayDeque<Op> opQueue = new ArrayDeque<>();
    private static final ElapsedTime opTimer = new ElapsedTime();

    private static double opsResumeAt = 0;
    private static boolean intakeRunning = false;

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
            if (!opQueue.isEmpty() || opTimer.milliseconds() < opsResumeAt) {
                stop();
                tickOps();
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
            if (start != null) {
                Double startRotation = program.startRotation;
                if (startRotation == null) {
                    // Face the center of the field from the start position.
                    startRotation = Math.atan2(50 - start[1], 50 - start[0]);
                }

                Coordination.setStart(start[0], start[1], startRotation);
                Logger.log("Autonomous: start heading " + startRotation + " rad");
            }
        });

        Scheduler.addTask("pre-run", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.AUTO) return;

            AutonomousProgram current = program;

            clearQueues();
            opQueue.addAll(expand(current.startActions));
            stepQueue.addAll(current.steps);

            Logger.log("Autonomous: loaded " + current.steps.size() + " waypoints");
            for (Step step : current.steps) {
                if (step.rotation != null) {
                    Logger.log("Autonomous: waypoint (" + step.x + ", " + step.y + ") heading " + step.rotation + " rad");
                }
            }
        });

        Scheduler.addTask("exit", (obj) -> {
            clearQueues();
            stop();
        });
    }


    /**
     * Queue a movement to {x, y, tolerance} in Coordination units, optionally
     * {x, y, tolerance, rotation} where rotation is a target heading in radians.
     */
    public static void requestMovement(double[] xyT){
        Double rotation = xyT.length > 3 ? xyT[3] : null;
        stepQueue.add(new Step(xyT[0], xyT[1], xyT[2], rotation, false, Collections.emptyList()));
    }

    public static void setProgram(AutonomousProgram program) {
        Autonomous.program = program;
    }

    public static AutonomousProgram getProgram() {
        return program;
    }

    public static void moveToPositionTick(Step step){
        cacheTarget(step);

        // Drive to the waypoint first, then rotate in place before running its actions
        if(!isWithinTolerance()){
            Drivetrain.stopRotating();
            setIntake(step.intake);

            puppet.setX(calculateSpeedForceX());
            puppet.setY(calculateSpeedForceY());
            return;
        }

        if(!isRotationWithinTolerance()){
            if (rf != null && !rf.equals(lastLoggedRotation)) {
                Logger.log("Autonomous: rotating to " + rf + " rad from " + r + " rad");
                lastLoggedRotation = rf;
            }

            setIntake(false);

            puppet.setX(0.00);
            puppet.setY(0.00);

            Drivetrain.rotateToward(rf, r, ROTATION_TOLERANCE);
            return;
        }

        stepQueue.remove(step);
        lastLoggedRotation = null;
        setIntake(false);
        opQueue.addAll(expand(step.actions));
        stop();
    }

    /**
     * Break actions down into hardware commands. The launcher spins up before the first of a run of
     * consecutive shots and spins down after the last, so back-to-back shots don't wait on spin-up.
     */
    private static List<Op> expand(List<Action> actions) {
        ArrayList<Op> ops = new ArrayList<>();
        boolean spinning = false;

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);

            switch (action.type) {
                case SHOOT:
                    ops.add(new Op(Op.Kind.LAUNCHER_ON, null, action.value));
                    if (!spinning) ops.add(new Op(Op.Kind.DELAY, null, LAUNCHER_SPIN_UP_MS));
                    spinning = true;

                    for (int shot = 0; shot < action.count; shot++) {
                        ops.add(new Op(Op.Kind.FEED_ON, action.piece, 0));
                        ops.add(new Op(Op.Kind.DELAY, null, FEED_MS));
                        ops.add(new Op(Op.Kind.FEED_OFF, action.piece, 0));
                        ops.add(new Op(Op.Kind.DELAY, null, FEED_GAP_MS));
                    }

                    boolean nextIsShot = i + 1 < actions.size() && actions.get(i + 1).type == Action.Type.SHOOT;
                    if (!nextIsShot) {
                        ops.add(new Op(Op.Kind.LAUNCHER_OFF));
                        spinning = false;
                    }
                    break;

                case INTAKE:
                    ops.add(new Op(Op.Kind.INTAKE_ON));
                    ops.add(new Op(Op.Kind.DELAY, null, action.value));
                    ops.add(new Op(Op.Kind.INTAKE_OFF));
                    break;

                case WAIT:
                    ops.add(new Op(Op.Kind.DELAY, null, action.value));
                    break;
            }
        }

        return ops;
    }

    private static void tickOps() {
        // Instant commands all run in the same tick, delays hold the queue until they pass
        while (!opQueue.isEmpty()) {
            if (opTimer.milliseconds() < opsResumeAt) return;

            Op op = opQueue.poll();

            switch (op.kind) {
                case LAUNCHER_ON:
                    Logger.log("Autonomous: launcher on at " + op.value + " deg");
                    setLaunchers(true, op.value);
                    break;
                case LAUNCHER_OFF:
                    setLaunchers(false, -1);
                    break;
                case FEED_ON:
                    Logger.log("Autonomous: shooting " + op.piece);
                    setFeed(op.piece, true);
                    break;
                case FEED_OFF:
                    setFeed(op.piece, false);
                    break;
                case INTAKE_ON:
                    setIntake(true);
                    break;
                case INTAKE_OFF:
                    setIntake(false);
                    break;
                case DELAY:
                    opsResumeAt = opTimer.milliseconds() + op.value;
                    break;
            }
        }
    }

    private static void setIntake(boolean on) {
        if (on == intakeRunning) return;
        intakeRunning = on;

        Intake.setPower(on ? 1 : 0);
        if (on) Storage.enableIntake();
        else Storage.disableIntake();
    }

    /** @param angle hood angle in degrees, or negative to leave it unchanged */
    private static void setLaunchers(boolean on, double angle) {
        for (String name : new String[] { "Launcher0", "Launcher1" }) {
            if (!Globals.has(name)) continue;

            Launcher launcher = Globals.depend(name);
            if (angle >= 0) launcher.setLaunchAngle(angle);

            if (on) launcher.enableLauncher();
            else launcher.disableLauncher();
        }
    }

    private static void setFeed(String piece, boolean on) {
        if (piece.equals("pollen")) {
            if (on) Storage.enablePollenServo();
            else Storage.disablePollenServo();
        } else {
            if (on) Storage.enableNectarServo();
            else Storage.disableNectarServo();
        }
    }

    /** Drop everything queued and leave every mechanism off. */
    private static void clearQueues() {
        opQueue.clear();
        stepQueue.clear();
        opsResumeAt = 0;
        lastLoggedRotation = null;

        Drivetrain.stopRotating();

        setFeed("pollen", false);
        setFeed("nectar", false);
        setLaunchers(false, -1);

        intakeRunning = true; // force the update so the intake is definitely off
        setIntake(false);
    }

    private static void stop(){
        Drivetrain.stopRotating();

        puppet.setX(0.00);
        puppet.setY(0.00);
    }


    private static void cacheTarget(Step target){
        x = Coordination.x;
        y = Coordination.y;
        r = Coordination.rotation;

        xf = target.x;
        yf = target.y;
        t = target.tolerance;
        rf = target.rotation;
    }

    private static boolean isWithinTolerance(){
        if(Math.abs(xf-x) <= t && Math.abs(yf-y) <= t) return true;
        return false;
    }

    private static boolean isRotationWithinTolerance(){
        if(rf == null) return true;
        return Math.abs(Drivetrain.getRotationError(rf, r)) <= ROTATION_TOLERANCE;
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
