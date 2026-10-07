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
    private ArrayList<DriverInputs> inputList = new ArrayList<DriverInputs>();
    private ArrayList<String> actionList = new ArrayList<String>();

    private HashMap<DriverInputs, Boolean> past = new HashMap<>();
    private HashMap<DriverInputs, Boolean> current = new HashMap<>();

    Driver(Gamepad gamepad) {
        this.gamepad = gamepad;

        UUID tick = Scheduler.addTask((obj) -> {
            this.tickControls();
        });

        Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(tick);
        });
    }

    public Driver addControl(DriverActions action, DriverInputs input) {
        inputList.add(input);
        actionList.add(action.toString());

        if (input.isToggle) {
            past.put(input, false);
            current.put(input, false);
        }

        return this;
    }

    public void tickControls() {
        DriverInputs currentInput;

        for (int i = 0; i < inputList.size(); i++) {
            currentInput = inputList.get(i);

            if (currentInput.isToggle) {
                if (past.get(currentInput) == current.get(currentInput)) {
                    Scheduler.trigger(actionList.get(i), null);
                }
                continue;
            }

            Scheduler.trigger(actionList.get(i), null);
        }
    }

    public double leftStickX() {
        return gamepad.left_stick_x;
    }
    public double leftStickY() {
        return gamepad.left_stick_y;
    }
}
