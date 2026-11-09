package org.pionerds.ftc.teamcode.Vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import android.util.Size;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import android.util.Log;

import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;
import org.openftc.apriltag.AprilTagPose;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Vision {
    private static final int CAMERA_WIDTH = 640;
    private static final int CAMERA_HEIGHT = 480;

    private static AprilTagProcessor aprilTagProcessor;
    private static AprilTagProcessor.Builder aprilTagProcessorBuilder;

    private static VisionPortal visionPortal;

    static {
        Scheduler.addTask("init", (obj) -> {
            if (!Environment.Capabilities.vision) return;

            aprilTagProcessorBuilder = new AprilTagProcessor.Builder();

            aprilTagProcessor = aprilTagProcessorBuilder
                    .setTagLibrary(AprilTagGameDatabase.getCurrentGameTagLibrary())
                    .setDrawAxes(true)
                    .setDrawTagOutline(true)
                    .setDrawTagID(true)
                    .build();


            Scheduler.addTask(0, (obj2) -> {

                Logger.log("Webcam: " + Hardware.webcam);

                if (Hardware.webcam == null) {
                    Logger.error("Webcam is null - check the hardware config name 'Webcam 1'");
                    return;
                }

                visionPortal = new VisionPortal.Builder()
                        .setCamera(Hardware.webcam)
                        .setCameraResolution(new Size(CAMERA_WIDTH, CAMERA_HEIGHT))
                        .setStreamFormat(VisionPortal.StreamFormat.MJPEG)
                        .enableLiveView(true)
                        .addProcessor(aprilTagProcessor)
                        .build();

                Scheduler.addTask(500, (obj3) -> {
                    Logger.log("Webcam: " + Hardware.webcam);
                    Logger.log("Webcam-Name: " + Hardware.webcam.getDeviceName());

                    Vision.LogAllViewableTags();
                });

            });
        });

        Scheduler.addTask("exit", (obj) -> {
            if (visionPortal != null) {
                visionPortal.close();
                visionPortal = null;
            }
        });
    }

    private AprilTagSingleDetection getSingleTagByID(int id) {
        ArrayList<AprilTagDetection> detections = aprilTagProcessor.getDetections();

        for (int i = 0; i < detections.size(); i++){
            if (!(detections.get(i) instanceof AprilTagSingleDetection)) continue;

            AprilTagSingleDetection detection = (AprilTagSingleDetection) detections.get(i);

            Logger.log("Found a single-tag with ID: " + detection.metadata.id);

            if (detection.metadata.id == id) {
                Logger.log("<" + id + "> found correctly!");
                return detection;
            }
        }
        return null;
    }

    private AprilTagClusterDetection getClusterTagByName(String name){
        ArrayList<AprilTagDetection> detections = aprilTagProcessor.getDetections();

        for (int i = 0; i < detections.size(); i++) {
            if (!(detections.get(i) instanceof AprilTagClusterDetection)) continue;

            AprilTagClusterDetection detection = (AprilTagClusterDetection) detections.get(i);

            Logger.log("Found a cluster named: " + detection.metadata.name);

            if (detection.metadata.name.equals(name)) {
                Logger.log("<" + name + "> found correctly!");
                return detection;
            }
        }
        return null;
    }

    private static void LogAllViewableTags(){
        if (visionPortal != null) {
            Logger.log("Camera-State: " + visionPortal.getCameraState() + " FPS: " + visionPortal.getFps());
        }

        ArrayList<AprilTagDetection> detections = aprilTagProcessor.getDetections();

        for (int i = 0; i < detections.size(); i++) {
            if (detections.get(i) instanceof AprilTagClusterDetection) {
                Logger.log("Bulk-Detection Metadata Found. Name: "+((AprilTagClusterDetection) detections.get(i)).metadata.name);
                Logger.log("Bulk-Detection Metadata Found. Short-Name: "+((AprilTagClusterDetection) detections.get(i)).metadata.shortName);
            } else if(detections.get(i) instanceof AprilTagSingleDetection) {
                Logger.log("Bulk-Detection Metadata Found. ID: "+((AprilTagSingleDetection) detections.get(i)).metadata.id);
                Logger.log("Bulk-Detection Metadata Found. Name: "+((AprilTagSingleDetection) detections.get(i)).metadata.name);
            }
        }

        Scheduler.addTask(500, (obj2) -> LogAllViewableTags());
    }
}
