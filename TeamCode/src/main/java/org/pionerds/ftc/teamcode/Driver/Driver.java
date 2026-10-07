package org.pionerds.ftc.teamcode.Driver;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Driver {

    private Gamepad gamepad;
    private static ArrayList<DriverInputs> inputList = new ArrayList<DriverInputs>();
    private ArrayList<String> actionList = new ArrayList<String>();

    private HashMap<DriverInputs, DriverActions> past = new HashMap<>();
    private HashMap<DriverInputs, DriverActions> current = new HashMap<>();

    Driver(Gamepad gamepad) {
        this.gamepad = gamepad;

        UUID tick = Scheduler.addTask((obj) -> {
            Driver.tickControls();
        });

        Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(tick);
        });
    }

    public Driver addControl(DriverActions action, DriverInputs input){
        inputList.add(input);
        actionList.add(action.toString());

        return this;
    }

    public static void tickControls(){
        for (int i = 0; i < inputList.size(); i++){
            if (1 == 1) { // write the condition for checking if a button press has happened

            }
        }
    }

    public double leftStickX() {
        return gamepad.left_stick_x;
    }
    public double leftStickY() {
        return gamepad.left_stick_y;
    }
}
