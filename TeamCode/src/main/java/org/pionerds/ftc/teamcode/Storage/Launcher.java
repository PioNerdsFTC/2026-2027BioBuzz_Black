package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Launcher {
    private String name;
    private DcMotorEx launchMotor;
    private Servo hoodServo;

    public Launcher(String name, String motorName, String launchServoName){
        launchMotor = Hardware.mapping.getMotor(motorName);
        hoodServo = Hardware.mapping.getServoMotor(launchServoName);

        if (launchMotor != null) launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        UUID rightTrigger = Scheduler.addTask("right-trigger", (trigger) -> {
            setLaunchAngle((Float) trigger * 180);
        });

        final boolean[] launcherToggled =  { false };

        UUID toggleLauncher = Scheduler.addTask("toggle-launcher:down", (obj) -> {
            if (launcherToggled[0]) disableLauncher();
            else enableLauncher();

            launcherToggled[0] = !launcherToggled[0];
        });

        AtomicReference<UUID> exit = new AtomicReference<>();

        exit.set(Scheduler.addTask("exit", (obj) -> {
            Scheduler.removeTask(rightTrigger);
            Scheduler.removeTask(toggleLauncher);
            Scheduler.removeTask(exit.get());
        }));
    }

    /**
     * @param theta hood angle in degrees, 0-180. Servo positions are 0-1.
     */
    public void setLaunchAngle(double theta) { if (hoodServo != null) hoodServo.setPosition(theta / 180); }

    public void enableLauncher() { if (launchMotor != null) launchMotor.setPower(1.0); }
    public void disableLauncher() { if (launchMotor != null) launchMotor.setPower(0.0); }
}
