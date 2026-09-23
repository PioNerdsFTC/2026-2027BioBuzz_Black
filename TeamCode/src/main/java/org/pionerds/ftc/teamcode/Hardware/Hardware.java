package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.function.Consumer;

/**
 * Class for all the hardware functions of the robot.
 * This should include helper classes and direct controllers for the hardware.
 */
public final class Hardware {

    public static ElapsedTime elapsedTime;
//    public Drivetrain drivetrain = new Drivetrain();
//    public Vision vision = new Vision();

    public Mapping mapping = new Mapping();
    public Gyro gyro = new Gyro();

    public static Telemetry telemetry = null;

    /**
     * Whether the hardware class is able to continue running.
     */
    public boolean continueRunning = true;

     public Hardware() {
         Scheduler.addTask("init", (o) -> {
             try {
                 HardwareMap hardwareMap = Globals.depend("hardware-map");
                 mapping.init(hardwareMap);
             } catch (Exception e) {
//                 telemetry.addLine(e.getMessage());
//                 telemetry.update();
             }
        });
    }

    /**
     * Runs for each iteration of the OpMode, may or may not be necessary
     */
//    public void tick(Gamepad gamepad1, Gamepad gamepad2) {
//        //try {
//        driverControls1.tickControls(gamepad1, this);
//        driverControls2.tickControls(gamepad2, this);
//
//        double[] angles = this.gyro.getAngles();
//        telemetry.addLine("Gyro:");
//        telemetry.addLine("Yaw: " + angles[0]);
//        // this.launcher.launcherButton(gamepad1);
//        //} catch (Exception e) {
//        //    this.telemetry.addLine(e.getMessage());
//        //    telemetry.update();
//        //if (!Environment.competing) {
//        //    telemetry.update();
//        //    this.continueRunning = false;
//        //}
//        //}
//    }

//    public void addElapsedTime(ElapsedTime elapsedTime) {
//        this.elapsedTime = elapsedTime;
//        ScheduleTask.initTime(elapsedTime);
//    }

//    public void sleep(double milliseconds){
//        double startTime = elapsedTime.milliseconds();
//        while (elapsedTime.milliseconds() < startTime+milliseconds && continueRunning){}
//    }
}