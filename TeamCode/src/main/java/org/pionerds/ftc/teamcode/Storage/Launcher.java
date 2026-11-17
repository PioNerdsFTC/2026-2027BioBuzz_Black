package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Vision.Vision;

public class Launcher {

    private String name;
    private DcMotorEx launchMotor;
    private Servo hoodServo;

    private boolean isEnabled = false;

    private double[] launcherOffsetFromCamera = new double[2];

    public Launcher(
        String name,
        String motorName,
        String launchServoName,
        boolean reverseDirection
    ) {
        launchMotor = Hardware.mapping.getMotor(motorName);
        if (launchMotor != null && reverseDirection) launchMotor.setDirection(
            DcMotorSimple.Direction.REVERSE
        );

        //hoodServo = Hardware.mapping.getServoMotor(launchServoName);

        if (launchMotor != null) {
            launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            launchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        /*UUID rightTrigger = Scheduler.addTask("right-trigger", trigger -> {
            setLaunchAngle((Float) trigger * 180);
        });*/

        final boolean[] launcherToggled = { false };

        UUID toggleLauncher = Scheduler.addTask("toggle-launcher:down", obj -> {
            if (launcherToggled[0]) disableLauncher();
            else enableLauncher();

            launcherToggled[0] = !launcherToggled[0];
        });

        UUID updateLauncher = Scheduler.addTask(obj -> {
            this.updateLaunchVelocity(Environment.Capabilities.aimbotEnabled);
        });

        AtomicReference<UUID> exit = new AtomicReference<>();

        exit.set(
            Scheduler.addTask("exit", obj -> {
                //Scheduler.removeTask(rightTrigger);
                Scheduler.removeTask(toggleLauncher);
                Scheduler.removeTask(updateLauncher);
                Scheduler.removeTask(exit.get());
            })
        );
    }

    public void setLauncherOffsetFromCamera(double[] launcherOffsetFromCamera){
        this.launcherOffsetFromCamera = launcherOffsetFromCamera;
    }

    /**
     * @param theta hood angle in degrees, 0-180. Servo positions are 0-1.
     */
    public void setLaunchAngle(double theta) {
        if (hoodServo != null) hoodServo.setPosition(theta / 180);
    }

    public void enableLauncher() {
        isEnabled = true;
    }

    public void disableLauncher() {
        isEnabled = false;
    }

    public boolean isEnabled(){ return isEnabled; }

    public boolean updateLaunchVelocity(boolean useAimbot){
        if(isEnabled) {
            if (Environment.Capabilities.vision && useAimbot) {
                AprilTagClusterDetection clusterDetection = Vision.getClusterTagByName("BLUE SCORING");
                if (clusterDetection != null && clusterDetection.ftcPose != null) {


                    double xi = launcherOffsetFromCamera[0];
                    double xf = clusterDetection.ftcPose.x;
                    double yi = launcherOffsetFromCamera[1];
                    double yf = clusterDetection.ftcPose.z;
                    double θ = 68 * Math.PI / 180;

                    launchMotor.setVelocity(Aimbot.getAngularVelocity(xi, xf, yi, yf, θ));
                    return true;
                } else {
                    launchMotor.setVelocity(20000);
                    return true;
                }
            }
        } else {
            launchMotor.setVelocity(0.00);
            return true;
        }
        return false;
    }
}
