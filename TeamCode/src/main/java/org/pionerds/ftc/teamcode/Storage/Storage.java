package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import java.util.ArrayList;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Storage {

    private static DcMotorEx intake0;
    private static DcMotorEx intake1;
    private static CRServo servoPollen0;
    private static CRServo servoPollen1;
    private static CRServo servoNectar0;
    private static CRServo servoNectar1;

    static {
        final boolean[] toggledIntake = { false };
        final boolean[] toggledPollenServo = { false };
        final boolean[] toggledNectar = { false };

        Scheduler.addTask("init", obj -> {
            intake0 = Hardware.mapping.getMotor("intake0");
            if (intake0 != null) intake0.setDirection(
                    DcMotorSimple.Direction.REVERSE
            );
            intake1 = Hardware.mapping.getMotor("intake1");
            if (intake1 != null) intake1.setDirection(
                DcMotorSimple.Direction.REVERSE
            );
            servoPollen0 = Hardware.mapping.getContinuousServo(
                "littleLaunchFeed0",
                DcMotorSimple.Direction.REVERSE
            );
            servoPollen1 = Hardware.mapping.getContinuousServo(
                "littleLaunchFeed1",
                DcMotorSimple.Direction.FORWARD
            );
            servoNectar0 = Hardware.mapping.getContinuousServo(
                "bigLaunchFeed0",
                DcMotorSimple.Direction.FORWARD
            );
            servoNectar1 = Hardware.mapping.getContinuousServo(
                "bigLaunchFeed1",
                DcMotorSimple.Direction.REVERSE
            );

            toggledIntake[0] = false;
            toggledPollenServo[0] = false;
            toggledNectar[0] = false;
        });

        Scheduler.addTask("toggle-intake:down", obj -> {
            if (toggledIntake[0]) disableIntake();
            else enableIntake();

            toggledIntake[0] = !toggledIntake[0];
        });

        Scheduler.addTask("pollen-up:down", obj -> {
            enablePollenServo();
        });

        Scheduler.addTask("pollen-up:up", obj -> {
            disablePollenServo();
        });

        Scheduler.addTask("pollen-down:down", obj -> {
            enablePollenServoReverse();
        });

        Scheduler.addTask("pollen-down:up", obj -> {
            disablePollenServo();
        });

        Scheduler.addTask("toggle-nectar:down", obj -> {
            if (toggledNectar[0]) disableNectarServo();
            else enableNectarServo();

            toggledNectar[0] = !toggledNectar[0];
        });
    }

    private final ArrayList<GamePiece> currentInventory = new ArrayList<>();

    public static void enableIntake() {
        if (intake0 != null) intake0.setPower(1.0);
        if (intake1 != null) intake1.setPower(1.0);
    }

    public static void disableIntake() {
        if (intake0 != null) intake0.setPower(0.0);
        if (intake1 != null) intake1.setPower(0.0);
    }

    public static void enablePollenServo() {
        if (servoPollen0 != null) servoPollen0.setPower(1.0);
        if (servoPollen1 != null) servoPollen1.setPower(1.0);
    }

    public static void enablePollenServoReverse() {
        if (servoPollen0 != null) servoPollen0.setPower(-1.0);
        if (servoPollen1 != null) servoPollen1.setPower(-1.0);
    }

    public static void disablePollenServo() {
        if (servoPollen0 != null) servoPollen0.setPower(0.0);
        if (servoPollen1 != null) servoPollen1.setPower(0.0);
    }

    public static void enableNectarServo() {
        if (servoNectar0 != null) servoNectar0.setPower(1.0);
        if (servoNectar1 != null) servoNectar1.setPower(1.0);
    }

    public static void disableNectarServo() {
        if (servoNectar0 != null) servoNectar0.setPower(0.0);
        if (servoNectar1 != null) servoNectar1.setPower(0.0);
    }

    // Currently Unused
    public void addElement(GamePiece gamePiece) {
        currentInventory.add(gamePiece);
    }

    // Currently Unused
    public void removeElement(GamePiece gamePiece) {
        for (GamePiece currentGamePiece : currentInventory) {
            if (gamePiece.equals(currentGamePiece)) {
                currentInventory.remove(gamePiece);
                return;
            }
        }
    }
}
