package org.pionerds.ftc.teamcode.Vision;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.openftc.apriltag.AprilTagPose;
import org.pionerds.ftc.teamcode.Scheduler.Scheduler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Vision {
    private static final boolean USE_WEBCAM = true;
    private static AprilTagProcessor aprilTagProcessor;
    private static VisionPortal visionPortal;

    // static final int VENDOR_ID_SUNPLUS_INNOVATION_TECHNOLOGY = 0x046d;
    // static final int PRODUCT_ID_ARDUCAM_OV5648 = 0x08e5;

    private HardwareMap hardwareMap;

    public static void init(HardwareMap hardwareMap){
        aprilTag = new AprilTagProcessor.Builder()

                //.setDrawAxes(false)
                //.setDrawCubeProjection(false)
                //.setDrawTagOutline(true)
                //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                //.setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)

                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        builder.addProcessor(aprilTag);
        visionPortal = builder.build();



    }

    ArrayList<DecoratedTag> currentDetections = new ArrayList<DecoratedTag>();

    public AprilTagPoseFtc getTagPosition(int id){

    }

    public void collectData(){
        ArrayList<AprilTagDetection> freshDetections = aprilTagProcessor.getFreshDetections();
        if(freshDetections != null){
            for(AprilTagDetection detection : freshDetections){
                currentDetections.add(new DecoratedTag(detection));
            }
        }
        for(aprilTagProcessor.get)
    }
}
