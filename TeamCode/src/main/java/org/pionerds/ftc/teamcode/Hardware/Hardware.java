package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Globals;
import org.pionerds.ftc.teamcode.Orchestration.Parameters;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.util.function.Consumer;

/**
 * Class for all the hardware functions of the robot.
 * This should include helper classes and direct controllers for the hardware.
 */
public final class Hardware {

    public Driver driver;

    public static Mapping mapping = new Mapping();
    public static Gyro gyro = new Gyro();

    public static Telemetry telemetry = null;

    static {
         Scheduler.addTask("init", (o) -> {
             try {
                 HardwareMap hardwareMap = Globals.depend("hardware-map");
                 Hardware.mapping.init(hardwareMap);
             } catch (Exception e) {
                 Logger.error(e.getMessage());
             }

             Scheduler.addTask((obj) -> {
                 Hardware.tick();
             });
        });

    }

    public static void tick() {
        //try {
//        Logger.clear();
//        Logger.log("running");
        if (!Parameters.running) return;
//        Logger.log("running x2");


        double[] angles = Hardware.gyro.getAngles();

        if (angles[0] != 0.0) {
            Logger.log("Yaw: " + angles[0]);
        }

        // this.launcher.launcherButton(gamepad1);
        //} catch (Exception e) {
        //    this.telemetry.addLine(e.getMessage());
        //    telemetry.update();
        //if (!Environment.competing) {
        //    telemetry.update();
        //    this.continueRunning = false;
        //}
        //}
    }

//    public void addElapsedTime(ElapsedTime elapsedTime) {
//        this.elapsedTime = elapsedTime;
//        ScheduleTask.initTime(elapsedTime);
//    }

//    public void sleep(double milliseconds){
//        double startTime = elapsedTime.milliseconds();
//        while (elapsedTime.milliseconds() < startTime+milliseconds && continueRunning){}
//    }
}