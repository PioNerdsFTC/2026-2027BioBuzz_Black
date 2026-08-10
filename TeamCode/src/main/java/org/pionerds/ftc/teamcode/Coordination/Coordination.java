package org.pionerds.ftc.teamcode.Coordination;

import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.pionerds.ftc.teamcode.Vision.DecoratedTag;
import org.pionerds.ftc.teamcode.Vision.Vision;

import java.util.HashMap;

public class Coordination {
    private static PioNerdPosition robotPosition;

    public static void cacheCoordinates(boolean calcY, boolean calcZ){
        AprilTagDetection[] availableTags = Vision.getCurrentDetections();

        double sumX = 0.00;
        double sumY = 0.00;
        double sumZ = 0.00;

        for(AprilTagDetection tag: availableTags){
            if(tag.ftcPose == null) continue;

            telemetry.addLine("\n");
            // sumX += |(x-dx)|
            if(getAbsoluteX(tag.id) > 0) sumX += Math.abs(getAbsoluteX(tag.id) - tag.ftcPose.x);

            telemetry.addLine("x = "+getAbsoluteX(tag.id));
            telemetry.addLine("dx = "+tag.ftcPose.x);
            telemetry.addLine("|(x-dx)| = "+ Math.abs(getAbsoluteX(tag.id) - tag.ftcPose.x));

            if(calcY && getAbsoluteY(tag.id) > 0) sumY += Math.abs(getAbsoluteY(tag.id) - tag.ftcPose.y);
            if(calcZ && getAbsoluteZ(tag.id) > 0) sumZ += Math.abs(getAbsoluteZ(tag.id) - tag.ftcPose.z);

        }

        telemetry.addLine("\n");
        telemetry.addLine("april tags: "+availableTags.length);

        double avgX = sumX/availableTags.length;
        double avgY = 0.00;
        double avgZ = 0.00;
        if(calcY){avgY = sumY/availableTags.length;}
        if(calcZ){avgZ = sumZ/availableTags.length;}

        telemetry.addLine("sumX = "+sumX);
        telemetry.addLine("avgX = "+avgX);

        robotPosition = new PioNerdPosition(avgX, avgY, avgZ);
    }

    // Store an absolute field position for each April Tag ID
    private static HashMap<Integer, PioNerdPosition> tagFieldPositions = new HashMap<>();

    static {
        // K, V ==> ID, Position
        tagFieldPositions.put(21, new PioNerdPosition(20.00,10.00,0.00));
        tagFieldPositions.put(22, new PioNerdPosition(37.00,10.00,0.00));

    }

    private static double getAbsoluteX(int id) {
        if(tagFieldPositions.get(id) != null) return tagFieldPositions.get(id).getX();
        else return -1000000.00;
    }
    private static double getAbsoluteY(int id) {
        if(tagFieldPositions.get(id) != null) return tagFieldPositions.get(id).getY();
        else return -1000000.00;
    }
    private static double getAbsoluteZ(int id) {
        if(tagFieldPositions.get(id) != null) return tagFieldPositions.get(id).getZ();
        else return -1000000.00;
    }

    public static PioNerdPosition getRobotPosition(){
        if(robotPosition != null) return robotPosition;
        return null;
    }





    private static Telemetry telemetry;
    public static void addTelemetry(Telemetry telemetry){Coordination.telemetry = telemetry;}
}
