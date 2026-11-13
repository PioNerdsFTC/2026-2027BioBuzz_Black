package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;

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

        Scheduler.addTask("init", (obj) -> {
            intake0 = Hardware.mapping.getMotor("intake0");
            intake1 = Hardware.mapping.getMotor("intake1");
                intake1.setDirection(DcMotorSimple.Direction.REVERSE);
            servoPollen0 = Hardware.mapping.getContinuousServo("littleLaunchFeed0", DcMotorSimple.Direction.FORWARD);
            servoPollen1 = Hardware.mapping.getContinuousServo("littleLaunchFeed1", DcMotorSimple.Direction.FORWARD);
            servoNectar0 = Hardware.mapping.getContinuousServo("bigLaunchFeed0", DcMotorSimple.Direction.FORWARD);
            servoNectar1 = Hardware.mapping.getContinuousServo("bigLaunchFeed1", DcMotorSimple.Direction.REVERSE);

            toggledIntake[0] = false;
            toggledPollenServo[0] = false;
            toggledNectar[0] = false;
        });

        Scheduler.addTask("toggle-intake:down", (obj) -> {
            if (toggledIntake[0]) disableIntake();
            else enableIntake();

            toggledIntake[0] = !toggledIntake[0];
        });

        Scheduler.addTask("toggle-pollen:down", (obj) -> {
            if (toggledPollenServo[0]) disablePollenServo();
            else enablePollenServo();

            toggledPollenServo[0] = !toggledPollenServo[0];
        });


        Scheduler.addTask("toggle-nectar:down", (obj) -> {
            if (toggledNectar[0]) disableNectarServo();
            else enableNectarServo();

            toggledNectar[0] = !toggledNectar[0];
        });
    }

    private final ArrayList<GamePiece> currentInventory = new ArrayList<>();

    public static void enableIntake() { if (intake0 != null && intake1 != null) { intake0.setPower(1.0); intake1.setPower(1.0); } }
    public static void disableIntake() { if (intake0 != null && intake1 != null) { intake0.setPower(0.0); intake1.setPower(0.0); } }
    public static void enablePollenServo() { if (servoPollen0 != null && servoPollen1 != null) { servoPollen0.setPower(1.0); servoPollen1.setPower(1.0); } }
    public static void disablePollenServo() { if (servoPollen0 != null && servoPollen1 != null) { servoPollen0.setPower(0.0); servoPollen1.setPower(0.0); } }
    public static void enableNectarServo() { if (servoNectar0 != null && servoNectar1 != null) { servoNectar0.setPower(1.0); servoNectar1.setPower(1.0); } }
    public static void disableNectarServo() { if (servoNectar0 != null && servoNectar1 != null) { servoNectar0.setPower(0.0); servoNectar1.setPower(0.0); } }


    // Currently Unused
    public void addElement(GamePiece gamePiece){currentInventory.add(gamePiece);}

    // Currently Unused
    public void removeElement(GamePiece gamePiece){
        for (GamePiece currentGamePiece : currentInventory) {
            if (gamePiece.equals(currentGamePiece)){
                currentInventory.remove(gamePiece);
                return;
            }
        }
    }
}
