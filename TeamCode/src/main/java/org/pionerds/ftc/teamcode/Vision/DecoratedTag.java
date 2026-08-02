package org.pionerds.ftc.teamcode.Vision;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
public class DecoratedTag {

    private AprilTagDetection aprilTagDetection;
    public DecoratedTag(AprilTagDetection aprilTagDetection){
        this.aprilTagDetection = aprilTagDetection;
    }

    public AprilTagDetection getTagDetection(){
        return aprilTagDetection;
    }

    public AprilTagPoseFtc getFTCPose(){
        return aprilTagDetection.ftcPose;
    }

    public double getX(){
        return aprilTagDetection.ftcPose.x;
    }

    public double getY(){
        return aprilTagDetection.ftcPose.y;
    }

    public double getZ() {
        return aprilTagDetection.ftcPose.z;
    }

}
