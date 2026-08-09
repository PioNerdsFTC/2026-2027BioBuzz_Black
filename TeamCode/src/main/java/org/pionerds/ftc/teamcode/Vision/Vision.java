package org.pionerds.ftc.teamcode.Vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.openftc.apriltag.AprilTagPose;
import org.pionerds.ftc.teamcode.Logging.Logger;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;
import org.pionerds.ftc.teamcode.Orchestration.Globals;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Vision {
    //private static final boolean USE_WEBCAM = true;
    private static AprilTagProcessor aprilTagProcessor;
    private static VisionPortal visionPortal;

    // static final int VENDOR_ID_SUNPLUS_INNOVATION_TECHNOLOGY = 0x046d;
    // static final int PRODUCT_ID_ARDUCAM_OV5648 = 0x08e5;

    private static HardwareMap hardwareMap;

    public static void init(HardwareMap hardwareMap){
        Vision.hardwareMap = hardwareMap;
        aprilTagProcessor = new AprilTagProcessor.Builder()

                //.setDrawAxes(false)
                //.setDrawCubeProjection(false)
                //.setDrawTagOutline(true)
                //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)

                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        builder.addProcessor(aprilTagProcessor);
        visionPortal = builder.build();



    }

    private static ArrayList<DecoratedTag> currentDetections = new ArrayList<>();
    private static ArrayList<Integer> currentDetectionIDs = new ArrayList<>();

    public static AprilTagPoseFtc getTagDisplacement(int id){
        return currentDetections.get(currentDetectionIDs.indexOf(id)).getPosition();
    }

    public static DecoratedTag[] getCurrentDetections(){
        return currentDetections.toArray(new DecoratedTag[0]);
    }

    // offsets for all April Tags to map them to our coordinate field are below as
    // x, y, z, yaw, pitch, roll, range, bearing, elevation
    private static double[] tagOffsets = {0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00};
    private static double[] tagScalars = {0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00};


    public static void collectData(){
        ArrayList<AprilTagDetection> freshDetections = aprilTagProcessor.getFreshDetections();
        if(freshDetections != null){
            for(AprilTagDetection detection : freshDetections){

                AprilTagPoseFtc unmodPos = detection.ftcPose;

                AprilTagPoseFtc modifiedPos = new AprilTagPoseFtc(
                        unmodPos.x * tagScalars[0] + tagOffsets[0],
                        unmodPos.y * tagScalars[1] + tagOffsets[1],
                        unmodPos.z * tagScalars[2] + tagOffsets[2],
                        unmodPos.yaw * tagScalars[3] + tagOffsets[3],
                        unmodPos.pitch * tagScalars[4] + tagOffsets[4],
                        unmodPos.roll * tagScalars[5] + tagOffsets[5],
                        unmodPos.range * tagScalars[6] + tagOffsets[6],
                        unmodPos.bearing * tagScalars[7] + tagOffsets[7],
                        unmodPos.elevation * tagScalars[8] + tagOffsets[8]
                );
                currentDetections.add(new DecoratedTag(detection,modifiedPos));
                currentDetectionIDs.add(detection.id);
            }
            Logger.warn("Cached new Tags!");
        } else {
            Logger.debug(Logger.LogType.DEBUG,"Did not cache.");
        }
    }
}
