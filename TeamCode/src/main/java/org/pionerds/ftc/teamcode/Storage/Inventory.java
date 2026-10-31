package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;

public class Inventory {

    private static DcMotorEx motorBottomInv;
    private static CRServo servoPollen;
    private static CRServo servoNectar1;
    private static CRServo servoNectar2;


    static {
        Scheduler.addTask("init", (Obj1) -> {
            motorBottomInv = Hardware.mapping.getMotor("motorBottomInv");
            servoPollen = Hardware.mapping.getContinuousServo("servoPollen", DcMotorSimple.Direction.FORWARD);
            servoNectar1 = Hardware.mapping.getContinuousServo("servoNectar1", DcMotorSimple.Direction.FORWARD);
            servoNectar2 = Hardware.mapping.getContinuousServo("servoNectar2", DcMotorSimple.Direction.REVERSE);
        });

        final boolean[] toggledIntake = { false };

        Scheduler.addTask("toggle-intake:down", (obj2) -> {
            if (toggledIntake[0]) disableIntake();
            else enableIntake();

            toggledIntake[0] = !toggledIntake[0];
        });
    }


    private final ArrayList<GamePiece> currentInventory = new ArrayList<>();

    public void enableIntake(){motorBottomInv.setPower(1.0);}
    public void disableIntake(){motorBottomInv.setPower(0.0);}
    public void enablePollenServo(){servoPollen.setPower(1.0);}
    public void disablePollenServo(){servoPollen.setPower(1.0);}
    public void enableNectarServo(){servoNectar1.setPower(1.0); servoNectar2.setPower(1.0);}
    public void disableNectarServo(){servoNectar1.setPower(0.0); servoNectar2.setPower(0.0);}


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
