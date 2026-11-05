package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;

public class Storage {

    private static DcMotorEx motorBottomInv;
    private static CRServo servoPollen;
    private static CRServo servoNectar1;
    private static CRServo servoNectar2;


    static {
        Scheduler.addTask("init", (obj) -> {
            motorBottomInv = Hardware.mapping.getMotor("motorBottomInv");
            servoPollen = Hardware.mapping.getContinuousServo("servoPollen", DcMotorSimple.Direction.FORWARD);
            servoNectar1 = Hardware.mapping.getContinuousServo("servoNectar1", DcMotorSimple.Direction.FORWARD);
            servoNectar2 = Hardware.mapping.getContinuousServo("servoNectar2", DcMotorSimple.Direction.REVERSE);
        });

        final boolean[] toggledIntake = { false };
        final boolean[] toggledPollenServo = { false };
        final boolean[] toggledNectar = { false };

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

    public static void enableIntake() { motorBottomInv.setPower(1.0); }
    public static void disableIntake() { motorBottomInv.setPower(0.0); }
    public static void enablePollenServo() { servoPollen.setPower(1.0); }
    public static void disablePollenServo() { servoPollen.setPower(1.0); }
    public static void enableNectarServo() { servoNectar1.setPower(1.0); servoNectar2.setPower(1.0); }
    public static void disableNectarServo() { servoNectar1.setPower(0.0); servoNectar2.setPower(0.0); }


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
