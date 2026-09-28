package org.pionerds.ftc.teamcode.Vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
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
    private static VisionPortal visionPortal;

    // static final int VENDOR_ID_SUNPLUS_INNOVATION_TECHNOLOGY = 0x046d;
    // static final int PRODUCT_ID_ARDUCAM_OV5648 = 0x08e5;

    private static HardwareMap hardwareMap;

    private static Telemetry telemetry;
    public static void addTelemetry(Telemetry telemetry){Vision.telemetry = telemetry;}

    static {
        Scheduler.addTask("init", (obj) -> {
            aprilTagProcessor = new AprilTagProcessor.Builder()
                    .setDrawAxes(true)
                    .setDrawCubeProjection(true)
                    .setDrawTagOutline(true)
                    //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                    //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                    .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)

                    .build();

            VisionPortal.Builder builder = new VisionPortal.Builder();

            builder.setCamera(Hardware.mapping.getWebcam("Webcam 1"));
            builder.addProcessor(aprilTagProcessor);

            visionPortal = builder.build();

            Scheduler.addTask((obj2) -> {
                Vision.collectData();
            });
        });
    }

    private static ArrayList<AprilTagDetection> currentDetections = new ArrayList<>();
    private static ArrayList<Integer> currentDetectionIDs = new ArrayList<>();

    public static AprilTagPoseFtc getTagFTCPose(int id){
        if (!currentDetectionIDs.contains(id)) return null;
        return currentDetections.get(currentDetectionIDs.indexOf(id)).ftcPose;
    }

    public static AprilTagDetection[] getCurrentDetections() {
        return currentDetections.toArray(new AprilTagDetection[0]);
    }

    // offsets for all April Tags to map them to our coordinate field are below as
    // x, y, z, yaw, pitch, roll, range, bearing, elevation
    private static double[] tagOffsets = { 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 };
    private static double[] tagScalars = { 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 };


    public static void collectData() {
        ArrayList<AprilTagDetection> freshDetections = aprilTagProcessor.getDetections();

        if (aprilTagProcessor.getDetections().isEmpty()) {
            Logger.error("Did not cache.");
            return;
        }

        currentDetections = new ArrayList<>();
        currentDetectionIDs = new ArrayList<>();

        for (AprilTagDetection detection : freshDetections) {
            if(detection.metadata == null) Logger.error("metadata is null");

            currentDetections.add(detection);
            currentDetectionIDs.add(detection.id);
        }

        Logger.log("Cached new Tags!");
    }
}
