package org.pionerds.ftc.teamcode.Driver;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.ArrayList;

public class Driver{

    private Gamepad gamepad;
    private ArrayList<DriverInputs> inputList = new ArrayList<DriverInputs>();
    private ArrayList<String> actionList = new ArrayList<String>();

    public Driver(Gamepad gamepad){
        this.gamepad = gamepad;
    }

    public Driver addControl(DriverActions action, DriverInputs input){
        inputList.add(input); actionList.add(action.toString());
        return this;
    }


    public void tickControls(){
        for (int i = 0; i< inputList.size(); i++){
            if(1==1){ // write the condition for checking if a button press has happened

            }
        }
    }
}
