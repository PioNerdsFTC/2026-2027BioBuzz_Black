package org.pionerds.ftc.teamcode.Vision;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc;
public class DecoratedTag {

    private AprilTagDetection aprilTagDetection;
    private AprilTagPoseFtc displacement;
    private int id;
    public DecoratedTag(AprilTagDetection aprilTagDetection, AprilTagPoseFtc displacement){
        this.aprilTagDetection = aprilTagDetection;
        this.id = aprilTagDetection.id;
        this.displacement = displacement;
    }

    public AprilTagDetection getTagDetection(){
        return aprilTagDetection;
    }

    public AprilTagPoseFtc getDisplacement(){
        return displacement;
    }

    public int getId(){return id;}

    public double getDeltaX(){return displacement.x;}
    public double getDeltaY(){return displacement.y;}
    public double getDeltaZ() {return displacement.z;}
    public double getDeltaYaw() {return displacement.yaw;}
    public double getDeltaPitch() {return displacement.pitch;}
    public double getDeltaRoll() {return displacement.roll;}
    public double getDeltaRange() {return displacement.range;}
    public double getDeltaBearing() {return displacement.bearing;}
    public double getDeltaElevation() {return displacement.elevation;}
}
