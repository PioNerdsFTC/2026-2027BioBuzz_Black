package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Action;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Step;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain._OmniDrivetrain;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Storage.Intake;
import org.pionerds.ftc.teamcode.Storage.Storage;

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

    /** Hardware commands that actions are broken down into. */
    private static final class Op {

        enum Kind {
            LAUNCHER_NECTAR_ON,
            LAUNCHER_NECTAR_OFF,
            LAUNCHER_POLLEN_ON,
            LAUNCHER_POLLEN_OFF,
            INTAKE_ON,
            INTAKE_OFF,
            DELAY,
        }

        final Kind kind;
        final double value;

        Op(Kind kind, String piece, double value) {
            this.kind = kind;
            this.value = value;
        }

        Op(Kind kind) {
            this(kind, null, 0);
        }
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

    static {
        _OmniDrivetrain.OVERRIDE_DRIVER(puppet);

        Scheduler.addTask(obj -> {
            if (
                Environment.Parameters.operatingEnvironment !=
                Environment.Parameters.OperatingEnvironment.AUTO
            ) return;
            if (!Scheduler.running) return;

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

        Scheduler.addTask("init", obj -> {
            if (
                Environment.Parameters.operatingEnvironment !=
                Environment.Parameters.OperatingEnvironment.AUTO
            ) return;

            double[] start = program.start;

            if (start != null) {
                Double startRotation = program.startRotation;

                if (startRotation == null) {
                    startRotation = Math.atan2(50 - start[1], 50 - start[0]);
                }

                Coordination.setStart(start[0], start[1], startRotation);

                Logger.log(
                    "Autonomous: start heading " + startRotation + " rad"
                );
            }
        });

        Scheduler.addTask("pre-run", obj -> {
            if (
                Environment.Parameters.operatingEnvironment !=
                Environment.Parameters.OperatingEnvironment.AUTO
            ) return;

            AutonomousProgram current = program;

            clearQueues();

            opQueue.addAll(expand(current.startActions));
            stepQueue.addAll(current.steps);

            Logger.log(
                "Autonomous: loaded " + current.steps.size() + " waypoints"
            );

            if (!Environment.Capabilities.otos && !current.steps.isEmpty()) {
                Logger.warn(
                    "Autonomous: no odometry, waypoints will never be reached"
                );
            }
        });

        Scheduler.addTask("exit", obj -> {
            clearQueues();
            stop();
        });
    }

    /**
     * Queue a movement to {x, y, tolerance} in Coordination units, optionally
     * {x, y, tolerance, rotation} where rotation is a target heading in radians.
     */
    public static void requestMovement(double[] xyT) {
        Double rotation = xyT.length > 3 ? xyT[3] : null;
        stepQueue.add(
            new Step(
                xyT[0],
                xyT[1],
                xyT[2],
                rotation,
                false,
                Collections.emptyList()
            )
        );
    }

    public static void setProgram(AutonomousProgram program) {
        Autonomous.program = program;
    }

    public static AutonomousProgram getProgram() {
        return program;
    }

    public static void moveToPositionTick(Step step) {
        cacheTarget(step);

        // implement drive.

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

                    if (!spinning) ops.add(
                        new Op(Op.Kind.DELAY, null, LAUNCHER_SPIN_UP_MS)
                    );

                    spinning = true;

                    ops.add(new Op(Op.Kind.DELAY, null, FEED_MS * action.count));

                    boolean nextIsShot =
                        i + 1 < actions.size() &&
                        actions.get(i + 1).type == Action.Type.SHOOT;

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
        while (!opQueue.isEmpty()) {
            if (opTimer.milliseconds() < opsResumeAt) return;

            Op op = opQueue.poll();

            switch (op.kind) {
                case LAUNCHER_ON:
                    Logger.log(
                        "Autonomous: launcher on at " + op.value + " deg"
                    );
                    setLaunchers(true, op.value);
                    AutonomousActions.launchNectar();
                    break;
                case LAUNCHER_OFF:
//                    setLaunchers(false, -1);
                    break;
                case INTAKE_ON:
//                    setIntake(true);
                    break;
                case INTAKE_OFF:
//                    setIntake(false);
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

            // use Actions
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

//        _Drivetrain.stopRotating();
//        _OmniDrivetrain.clearFieldVector();

        setFeed("pollen", false);
        setFeed("nectar", false);
        setLaunchers(false, -1);

        intakeRunning = true;
        setIntake(false);
    }

    private static void stop() {
//        _Drivetrain.stopRotating();
//        _OmniDrivetrain.clearFieldVector();

        puppet.setX(0.00);
        puppet.setY(0.00);
    }

    private static void cacheTarget(Step target) {
        x = Coordination.x;
        y = Coordination.y;
        r = Coordination.rotation;

        xf = target.x;
        yf = target.y;
        t = target.tolerance;
        rf = target.rotation;
    }

    private static double getΔ(double p2, double p1) {
        return Math.abs(p2 - p1);
    }

    private static double calculateSpeedForceX() {
        return calculateSpeedForce(xf, x);
    }

    private static double calculateSpeedForceY() {
        return calculateSpeedForce(yf, y);
    }

    private static double calculateSpeedForce(double target, double current) {
        double Δ = getΔ(target, current);
        double direction = Math.signum(target - current);

        if (Δ <= t) return 0.00;
        else if (Δ >= 20) return direction * maxSpeed;
        else if (Δ >= 10) return direction * mediumSpeed;
        else return direction * slowSpeed;
    }
}
