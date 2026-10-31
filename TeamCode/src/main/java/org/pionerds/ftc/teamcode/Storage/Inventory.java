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
    private static CRServo servoNectar;


    static {
        Scheduler.addTask("init", (Obj1) -> {
            motorBottomInv = Hardware.mapping.getMotor("motorBottomInv");
            servoPollen = Hardware.mapping.getContinuousServo("servoPollen", DcMotorSimple.Direction.FORWARD);
            servoNectar = Hardware.mapping.getContinuousServo("servoNectar", DcMotorSimple.Direction.FORWARD);
        });
    }


    private final ArrayList<GamePiece> currentInventory = new ArrayList<GamePiece>();

    public void enableIntake(){motorBottomInv.setPower(1.0);}
    public void disableIntake(){motorBottomInv.setPower(0.0);}
    public void enablePollenServo(){servoPollen.setPower(1.0);}
    public void disablePollenServo(){servoPollen.setPower(1.0);}
    public void enableNectarServo(){servoNectar.setPower(1.0);}
    public void disableNectarServo(){servoNectar.setPower(0.0);}

    public void addElement(GamePiece gamePiece){currentInventory.add(gamePiece);}
    public void removeElement(GamePiece gamePiece){
        for (GamePiece currentGamePiece : currentInventory) {
            if (gamePiece.equals(currentGamePiece)){
                currentInventory.remove(gamePiece);
                return;
            }
        }
    }

}
