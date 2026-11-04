package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.pionerds.ftc.teamcode.Hardware.Hardware;

public class Launcher {

    private String name;
    private DcMotorEx launchMotor;
    private Servo hoodServo;


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
