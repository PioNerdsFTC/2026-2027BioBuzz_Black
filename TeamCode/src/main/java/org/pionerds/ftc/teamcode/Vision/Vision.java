package org.pionerds.ftc.teamcode.Vision;

import android.util.Log;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.openftc.apriltag.AprilTagPose;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Vision {
    private static AprilTagProcessor aprilTagProcessor;
    private static AprilTagProcessor.Builder aprilTagProcessorBuilder;

    private static VisionPortal visionPortal;

    static {
        Scheduler.addTask("init", (obj) -> {
            aprilTagProcessorBuilder = new AprilTagProcessor.Builder();
            aprilTagProcessorBuilder.setTagLibrary(AprilTagGameDatabase.getCurrentGameTagLibrary());

            aprilTagProcessor = aprilTagProcessorBuilder.build();

            testDetections();
        });
    }

    private static void testDetections() {
        Scheduler.addTask(500, (obj) -> {
            Logger.log("Found " + getCurrentDetections().size() + " detection(s)");
            testDetections();
        });
    }

    // Returns the detections arrayList and updates the detection IDs
    public static ArrayList<AprilTagDetection> getCurrentDetections() {
        return aprilTagProcessor.getDetections();
    }

    // offsets for all April Tags to map them to our coordinate field are below as
    // x, y, z, yaw, pitch, roll, range, bearing, elevation
    //private static double[] tagOffsets = { 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 };
    //private static double[] tagScalars = { 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 };
}
