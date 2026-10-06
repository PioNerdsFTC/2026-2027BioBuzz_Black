package org.pionerds.ftc.teamcode.Driver;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.ArrayList;

public class Driver {

    private Gamepad gamepad;
    private ArrayList<DriverInputs> inputList = new ArrayList<DriverInputs>();
    private ArrayList<DriverActions> actionList = new ArrayList<DriverActions>();

    public Driver(Gamepad gamepad){
        this.gamepad = gamepad;
    }

    public Driver addControl(DriverActions action, DriverInputs input){
        inputList.add(input); actionList.add(action);
        return this;
    }


    public void tickControls(){

    }

}
