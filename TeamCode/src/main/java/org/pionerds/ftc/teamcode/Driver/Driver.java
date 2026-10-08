package org.pionerds.ftc.teamcode.Driver;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Driver {

    private Gamepad gamepad;
    private ArrayList<DriverInput> inputList = new ArrayList<>();
    private ArrayList<String> actionList = new ArrayList<>();

    private HashMap<DriverInput, Boolean> past = new HashMap<>();
    private HashMap<DriverInput, Boolean> current = new HashMap<>();

    public Driver(Gamepad gamepad) {
        this.gamepad = gamepad;

        UUID tick = Scheduler.addTask((obj) -> {
            this.tickControls();
        });

        Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(tick);
        });
    }

    public Driver addControl(DriverInput input, DriverAction action) {
        inputList.add(input);
        actionList.add(action.toString());

        if (input.isToggle) {
            past.put(input, false);
            current.put(input, false);
        }

        return this;
    }

    public void tickControls() {
        DriverInput currentInput;

        for (int i = 0; i < inputList.size(); i++) {
            currentInput = inputList.get(i);

            if (currentInput.isToggle) {
                past.put(currentInput, current.get(currentInput));
                past.put(currentInput, getMappedInput(currentInput));

                if (past.get(currentInput) == current.get(currentInput)) {
                    Scheduler.trigger(actionList.get(i), null);
                }
                continue;
            }

            if (!this.getMappedInput(currentInput)) return;

            Scheduler.trigger(actionList.get(i), null);
        }
    }

    public double leftStickX() {
        return gamepad.left_stick_x;
    }
    public double leftStickY() {
        return gamepad.left_stick_y;
    }

    private boolean getMappedInput(DriverInput input) {
        switch (input) {
            case LEFT_STICK_Y:
                return false; // Not a boolean
            case LEFT_STICK_X:
                return false; // Not a boolean
            case RIGHT_STICK_Y:
                return false; // Not a boolean
            case RIGHT_STICK_X:
                return false; // Not a boolean
            case A:
                return gamepad.a;
            case B:
                return gamepad.b;
            case X:
                return gamepad.x;
            case Y:
                return gamepad.y;
            case RIGHT_TRIGGER:
                return false; // not a boolean
            case LEFT_TRIGGER:
                return false; // not a boolean
            case RIGHT_BUMPER:
                return gamepad.right_bumper;
            case LEFT_BUMPER:
                return gamepad.left_bumper;
            case LEFT_STICK_BUTTON:
                return gamepad.left_stick_button;
            case RIGHT_STICK_BUTTON:
                return gamepad.right_stick_button;
            case DPAD_UP:
                return gamepad.dpad_up;
            case DPAD_DOWN:
                return gamepad.dpad_down;
            case DPAD_RIGHT:
                return gamepad.dpad_right;
            case DPAD_LEFT:
                return gamepad.dpad_left;
            default:
                return false;
        }
    }
}
