package org.pionerds.ftc.teamcode.Storage;

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

    }

    public void setLaunchAngle(double theta){
        hoodServo.setPosition(theta);
    }

}
