package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.json.JSONException;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Action;
import org.pionerds.ftc.teamcode.Autonomous.AutonomousProgram.Step;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.Drivetrain;
import org.pionerds.ftc.teamcode.Drivetrain.RobotCentricDrivetrain;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

/**
 * Runs the dashboard's {@link AutonomousProgram}: drives to each waypoint, then runs its actions.
 * Actions are in {@link AutonomousActions}. This class is the orchestrator of AutonomousActions & AutonomousPrograms
 */
public class Autonomous {

    /** How close the robot has to be to a waypoint's heading, in radians. */
    private static final double ROTATION_TOLERANCE = Math.toRadians(2);

    /** How long the launcher needs to reach speed before the first shot. */
    private static final double LAUNCHER_SPIN_UP_MS = 1000;
    /** How long the feed servo runs per piece launched. */
    private static final double FEED_MS = 400;

    /** How often to log movement progress while driving to a waypoint. */
    private static final double MOVEMENT_LOG_INTERVAL_MS = 250;

    private static final ElapsedTime movementLogTimer = new ElapsedTime();

    private static double lastLogTargetXCm = Double.NaN;
    private static double lastLogTargetYCm = Double.NaN;
    private static double lastLogDistanceCm = Double.NaN;

    /** Hardware commands that actions are broken down into. */
    private static final class Op {

        enum Kind {
            LAUNCH_ON,
            LAUNCH_OFF,
            INTAKE_ON,
            INTAKE_OFF,
            DELAY,
        }

        final Kind kind;
        /** LAUNCH_ON / LAUNCH_OFF: "pollen" or "nectar". */
        final String piece;
        /** LAUNCH_ON: launcher speed, 0 for the launcher's default. DELAY: milliseconds. */
        final double value;

        Op(Kind kind, String piece, double value) {
            this.kind = kind;
            this.piece = piece;
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

    private static volatile AutonomousProgram program = AutonomousProgram.EMPTY;


    private static final File file = AppUtil.getInstance().getSettingsFile("auto-program");

    /**
     * Master flag for whether Auto will run or not. If true, or set to true in operation, Auto will stop executing anything.
     */
    private static boolean disabled = false;

    static {
        Scheduler.addTask(obj -> {
            if (
                disabled ||
                Environment.Parameters.emergencyAuto ||
                !isAuto() ||
                !Scheduler.running
            ) {
                return;
            }

            if (!Environment.Capabilities.otos) {
                Logger.error("!!! OTOS suddenly not available. Stopping Auto !!!");
                disabled = true;
            }

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

        // Occurs when otos lost, then reaquired.
        Scheduler.addTask("hardware:enable:otos", obj -> {
            disabled = !selfTest();
        });


        Scheduler.addTask("pre-run", obj -> {
            if (Environment.Parameters.emergencyAuto || !isAuto()) {
                disabled = true;
                return;
            }

            if (!Environment.Capabilities.otos) {
                Logger.error("Autonomous will not run due to the Odometer not being available");
                disabled = true;
                return;
            }

            if (program.equals(AutonomousProgram.EMPTY)) {
                String res = ReadWriteFile.readFile(file);

                try {
                    program = AutonomousProgram.fromJson(res);
                } catch (JSONException e) {
                    Logger.warn("Autonomous: invalid autonomous program saved to disk ???");
                }
            }

            AutonomousProgram current = program;

            double[] start = current.start;

            if (start != null) {
                Double startRotation = current.startRotation;

                if (startRotation == null) {
                    startRotation = Math.atan2(50 - start[1], 50 - start[0]);
                }

                Coordination.setStart(start[0], start[1], startRotation);

                Logger.log(
                    "Autonomous: start heading " + startRotation + " rad"
                );
            }

            Drivetrain.overrideDriver(AutonomousActions.puppetDriver);
            RobotCentricDrivetrain.overrideDriver(AutonomousActions.puppetDriver);

            clearQueues();

            opQueue.addAll(expand(current.startActions));
            stepQueue.addAll(current.steps);

            Logger.log(
                "Autonomous: loaded " + current.steps.size() + " waypoints"
            );
        });

        Scheduler.addTask("exit", obj -> {
            disabled = false;

            if (!isAuto()) return;

            clearQueues();
        });
    }

    private static boolean selfTest() {
        if (
                Environment.Capabilities.otos &&
                Environment.Capabilities.drivetrain
        )  return true;
        return false;
    }

    private static boolean isAuto() {
        return (
            Environment.Parameters.operatingEnvironment ==
            Environment.Parameters.OperatingEnvironment.AUTO
        );
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

        ReadWriteFile.writeFile(file, program.toJson());

        double[] start = program.start;
        if (start == null) return;

        Double rotation = program.startRotation;
        if (rotation == null) {
            rotation = Math.atan2(50 - start[1], 50 - start[0]);
        }

        Coordination.setStart(start[0], start[1], rotation);
    }

    public static AutonomousProgram getProgram() {
        return program;
    }

    /** Drive toward the step's position, then its heading, then queue its actions. */
    public static void moveToPositionTick(Step step) {
        PuppetDriver puppet = AutonomousActions.puppetDriver;

        if (!isPositionReached(step)) {
            setIntake(step.intake);
            puppet.setRotation(0.00);
            drivePositionToward(step);
            return;
        }

        if (!isRotationReached(step)) {
            setIntake(false);
            puppet.setX(0.00);
            puppet.setY(0.00);
            driveRotationToward(step.rotation);
            return;
        }

        stop();
        setIntake(false);
        stepQueue.remove(0);
        opQueue.addAll(expand(step.actions));

        Logger.log(
            "Autonomous: reached (" + step.x + ", " + step.y + "), " +
                stepQueue.size() + " waypoints left"
        );
    }

    /**
     * Waypoints arrive from the dashboard in Coordination units (0-100), but the drive loop works
     * in centimeters, matching the odometer. Convert both the target and its tolerance here, then
     * let {@link AutonomousActions#driveToPosition} convert the field error into the drivetrain's
     * frame via {@link Coordination#fieldToRobot}.
     */
    private static void drivePositionToward(Step step) {
        double targetXCm = Coordination.unitsToCm(step.x);
        double targetYCm = Coordination.unitsToCm(step.y);
        double currentXCm = Coordination.xCm();
        double currentYCm = Coordination.yCm();

        double deltaXCm = targetXCm - currentXCm;
        double deltaYCm = targetYCm - currentYCm;
        double distanceCm = Math.hypot(deltaXCm, deltaYCm);

        AutonomousActions.driveToPosition(
            targetXCm,
            targetYCm,
            Coordination.unitsToCm(step.tolerance)
        );
    }

    private static void driveRotationToward(double target) {
        AutonomousActions.driveToRotation(
            target,
            ROTATION_TOLERANCE
        );
    }

    private static boolean isPositionReached(Step step) {
        double tolerance = Coordination.unitsToCm(step.tolerance);

        return (
            Math.abs(Coordination.unitsToCm(step.x) - Coordination.xCm()) <=
                tolerance &&
            Math.abs(Coordination.unitsToCm(step.y) - Coordination.yCm()) <=
                tolerance
        );
    }

    private static boolean isRotationReached(Step step) {
        if (step.rotation == null) return true;

        return (
            Math.abs(normalizeAngle(step.rotation - Coordination.rotation)) <=
            ROTATION_TOLERANCE
        );
    }

    /**
     * Break actions down into hardware commands.
     */
    private static List<Op> expand(List<Action> actions) {
        ArrayList<Op> ops = new ArrayList<>();
        String spinning = null;

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);

            switch (action.type) {
                case SHOOT:
                    ops.add(new Op(Op.Kind.LAUNCH_ON, action.piece, action.value));

                    if (!action.piece.equals(spinning)) ops.add(
                        new Op(Op.Kind.DELAY, null, LAUNCHER_SPIN_UP_MS)
                    );

                    spinning = action.piece;

                    ops.add(new Op(Op.Kind.DELAY, null, FEED_MS * action.count));

                    boolean nextIsSameShot =
                        i + 1 < actions.size() &&
                        actions.get(i + 1).type == Action.Type.SHOOT &&
                        actions.get(i + 1).piece.equals(action.piece);

                    if (!nextIsSameShot) {
                        ops.add(new Op(Op.Kind.LAUNCH_OFF, action.piece, 0));
                        spinning = null;
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
                case LAUNCH_ON:
                    Logger.log(
                        "Autonomous: launching " + op.piece +
                            (op.value > 0 ? " at " + op.value : " at default speed")
                    );
                    setLauncher(op.piece, true, op.value);
                    break;
                case LAUNCH_OFF:
                    setLauncher(op.piece, false, 0);
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

    /** @param speed launcher speed, or 0 for the launcher's default */
    private static void setLauncher(String piece, boolean on, double speed) {
        boolean pollen = piece.equals("pollen");

        if ((pollen ? AutonomousActions.launcherPollen : AutonomousActions.launcherNectar) == null) return;

        if (pollen) {
            if (!on) AutonomousActions.stopLaunchingPollen();
            else if (speed > 0) AutonomousActions.launchPollen(speed);
            else AutonomousActions.launchPollen();
        } else {
            if (!on) AutonomousActions.stopLaunchingNectar();
            else if (speed > 0) AutonomousActions.launchNectar(speed);
            else AutonomousActions.launchNectar();
        }
    }

    private static void setIntake(boolean on) {
        if (on == intakeRunning) return;
        intakeRunning = on;

        if (on) AutonomousActions.enableIntake();
        else AutonomousActions.disableIntake();
    }

    /** Drop everything queued and leave every mechanism off. */
    private static void clearQueues() {
        opQueue.clear();
        stepQueue.clear();
        opsResumeAt = 0;

        resetMovementLog();

        setLauncher("pollen", false, 0);
        setLauncher("nectar", false, 0);

        intakeRunning = true;
        setIntake(false);

        stop();
    }

    private static void resetMovementLog() {
        lastLogTargetXCm = Double.NaN;
        lastLogTargetYCm = Double.NaN;
        lastLogDistanceCm = Double.NaN;
        movementLogTimer.reset();
    }

    private static void stop() {
        PuppetDriver puppet = AutonomousActions.puppetDriver;
        if (puppet == null) return;

        puppet.setX(0.00);
        puppet.setY(0.00);
        puppet.setRotation(0.00);
    }

    private static double normalizeAngle(double angle) {
        double twoPi = 2 * Math.PI;

        angle %= twoPi;
        if (angle > Math.PI) angle -= twoPi;
        if (angle < -Math.PI) angle += twoPi;

        return angle;
    }
}
