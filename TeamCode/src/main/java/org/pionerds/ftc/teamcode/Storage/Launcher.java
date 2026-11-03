package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Hardware.Hardware;

public class Launcher {

    private String name;
    private DcMotorEx launchMotor;


    public Launcher(String name, String motorName){
        launchMotor = Hardware.mapping.getMotor(motorName);
    }

}
