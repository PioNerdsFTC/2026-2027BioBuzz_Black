package org.pionerds.ftc.teamcode.Vision;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
public class DecoratedTag {

    private AprilTagDetection aprilTagDetection;
    private AprilTagPoseFtc position;
    public DecoratedTag(AprilTagDetection aprilTagDetection, AprilTagPoseFtc position){
        this.aprilTagDetection = aprilTagDetection;
        this.position = position;
    }

    public AprilTagDetection getTagDetection(){
        return aprilTagDetection;
    }

    public AprilTagPoseFtc getPosition(){
        return position;
    }

    public double getX(){return position.x;}
    public double getY(){return position.y;}
    public double getZ() {return position.z;}
    public double getYaw() {return position.yaw;}
    public double getPitch() {return position.pitch;}
    public double getRoll() {return position.roll;}
    public double getRange() {return position.range;}
    public double getBearing() {return position.bearing;}
    public double getElevation() {return position.elevation;}
}
