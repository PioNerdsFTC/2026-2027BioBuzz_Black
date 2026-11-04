package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Launcher {

    private String name;
    private DcMotorEx launchMotor;
    private Servo hoodServo;


    static {

        Scheduler.addTask("init", (Obj)->{
            Globals.add("Launcher1",new Launcher("Launcher0","launcher0","launchHood0"));
            Globals.add("Launcher1",new Launcher("Launcher1","launcher1","launchHood1"));
        });

    }


    public Launcher(String name, String motorName, String launchServoName){
        launchMotor = Hardware.mapping.getMotor(motorName);
        hoodServo = Hardware.mapping.getServoMotor(launchServoName);

        launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void setLaunchAngle(double theta){
        hoodServo.setPosition(theta - 180);

    }

    public void enableLauncher(){
        launchMotor.setPower(1.0);
    }

    public void disableLauncher(){
        launchMotor.setPower(0.0);
    }

}
