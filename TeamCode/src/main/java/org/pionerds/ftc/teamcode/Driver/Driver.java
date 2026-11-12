package org.pionerds.ftc.teamcode.Driver;

import android.annotation.SuppressLint;
import android.util.Log;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Driver {

    private Gamepad gamepad;
    private ArrayList<DriverInput> inputList = new ArrayList<>();
    private ArrayList<String> actionList = new ArrayList<>();
    private ArrayList<Boolean> toggleList = new ArrayList<>();

    private HashMap<DriverInput, Boolean> past = new HashMap<>();
    private HashMap<DriverInput, Boolean> current = new HashMap<>();

    public Driver(Gamepad gamepad) {
        this.gamepad = gamepad;

        UUID tick = Scheduler.addTask((obj) -> {
            if (!Scheduler.running) return;

            this.tickControls();
        });

        AtomicReference<UUID> exit = new AtomicReference<>();

        exit.set(Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(tick);
            Scheduler.removeTask(exit.get());
        }));
    }

    public Driver addControl(DriverInput input, DriverAction action, boolean isToggle) {
        inputList.add(input);
        actionList.add(action.toString());
        toggleList.add(isToggle);

        if (isToggle) {
            past.put(input, false);
            current.put(input, false);
        }

        return this;
    }

    @SuppressLint("UseValueOf")
    public void tickControls() {
        if (gamepad == null) return;

        DriverInput currentInput;

        for (int i = 0; i < inputList.size(); i++) {
            currentInput = inputList.get(i);

            if (toggleList.get(i)) {
                past.put(currentInput, current.get(currentInput));
                current.put(currentInput, getMappedInput(currentInput));

                if (past.get(currentInput) != current.get(currentInput)) {
                    if (getMappedInput(currentInput)) {
                        Scheduler.trigger(actionList.get(i) + ":down", null);
                    }
                    if (!getMappedInput(currentInput)) {
                        Scheduler.trigger(actionList.get(i) + ":up", null);
                    }
                }
                continue;
            }

            if (!this.getMappedInput(currentInput)) continue;

            Scheduler.trigger(actionList.get(i), null);
        }

        // TODO: Determine perf impact.
        Scheduler.trigger("right-trigger", gamepad.right_trigger);
    }

    public double leftStickX() {
        return nonLinearScaleStick(gamepad.left_stick_x);
    }
    public double leftStickY() {
        return nonLinearScaleStick(gamepad.left_stick_y);
    }

    public double nonLinearScaleStick(double input){
        return Math.pow(input,2)*Math.signum(input);
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
