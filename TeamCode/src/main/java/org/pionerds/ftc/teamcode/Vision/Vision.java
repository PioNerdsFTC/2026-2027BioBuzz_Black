package org.pionerds.ftc.teamcode.Vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;
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


            Scheduler.addTask(0, (obj2) -> {

                Logger.log("Webcam: " + Hardware.webcam);

                if (Hardware.webcam == null) {
                    Logger.error("Webcam is null - check the hardware config name 'Webcam 1'");
                    return;
                }

                visionPortal = VisionPortal.easyCreateWithDefaults(Hardware.webcam, aprilTagProcessor);

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

    private AprilTagSingleDetection getSingleTagByID(int id){
        for(int i = 0; i<aprilTagProcessor.getDetections().size(); i++){
            if(aprilTagProcessor.getDetections().get(i) instanceof AprilTagSingleDetection){

                AprilTagSingleDetection detection = (AprilTagSingleDetection) aprilTagProcessor.getDetections().get(i);

                Logger.log("Found a single-tag with ID: "+detection.metadata.id);

                if(detection != null && detection.metadata != null && detection.metadata.id == id){
                    Logger.log("<"+id+"> found correctly!");
                    return detection;
                }

            }
        }
        return null;
    }

    private AprilTagClusterDetection getClusterTagByName(String name){
        for(int i = 0; i<aprilTagProcessor.getDetections().size(); i++){
            if(aprilTagProcessor.getDetections().get(i) instanceof AprilTagClusterDetection){

                AprilTagClusterDetection detection = (AprilTagClusterDetection) aprilTagProcessor.getDetections().get(i);

                Logger.log("Found a cluster named: "+detection.metadata.name);

                if(detection != null && detection.metadata.name.equals(name)){
                    Logger.log("<"+name+"> found correctly!");
                    return detection;
                }

            }
        }
        return null;
    }

    public static void LogAllViewableTags(){
        for(int i = 0; i<aprilTagProcessor.getDetections().size(); i++){
            if(aprilTagProcessor.getDetections().get(i) instanceof AprilTagClusterDetection) {
                Logger.log("Bulk-Detection Metadata Found. Name: "+((AprilTagClusterDetection) aprilTagProcessor.getDetections().get(i)).metadata.name);
                Logger.log("Bulk-Detection Metadata Found. Short-Name: "+((AprilTagClusterDetection) aprilTagProcessor.getDetections().get(i)).metadata.shortName);
            } else if(aprilTagProcessor.getDetections().get(i) instanceof AprilTagSingleDetection) {
                Logger.log("Bulk-Detection Metadata Found. ID: "+((AprilTagSingleDetection) aprilTagProcessor.getDetections().get(i)).metadata.id);
                Logger.log("Bulk-Detection Metadata Found. Name: "+((AprilTagSingleDetection) aprilTagProcessor.getDetections().get(i)).metadata.name);
            }

        }

        Scheduler.addTask(500, (obj2) -> {LogAllViewableTags();});
    }
}
