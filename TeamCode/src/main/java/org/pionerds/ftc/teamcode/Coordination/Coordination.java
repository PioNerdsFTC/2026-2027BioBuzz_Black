package org.pionerds.ftc.teamcode.Coordination;

import com.sun.tools.javac.util.Position;

import org.pionerds.ftc.teamcode.Vision.DecoratedTag;
import org.pionerds.ftc.teamcode.Vision.Vision;

import java.util.HashMap;

public class Coordination {
    private static PioNerdPosition robotPosition;

    public static void cacheCoordinates(boolean calcY, boolean calcZ){
        DecoratedTag[] availableTags = Vision.getCurrentDetections();

        double sumX = 0.00;
        double sumY = 0.00;
        double sumZ = 0.00;

        for(DecoratedTag tag: availableTags){
            // sumX += |(x-dx)|
            sumX += Math.abs(getAbsoluteX(tag.getId()) - tag.getDeltaX());

            if(calcY) sumY += Math.abs(getAbsoluteY(tag.getId()) - tag.getDeltaY());
            if(calcZ) sumZ += Math.abs(getAbsoluteZ(tag.getId()) - tag.getDeltaZ());

        }

        double avgX = sumX/availableTags.length;
        double avgY = 0.00;
        double avgZ = 0.00;
        if(calcY){avgY = sumY/availableTags.length;}
        if(calcZ){avgZ = sumZ/availableTags.length;}

        robotPosition = new PioNerdPosition(avgX, avgY, avgZ);
    }

    // Store an absolute field position for each April Tag ID
    private static HashMap<Integer, PioNerdPosition> tagFieldPositions = new HashMap<>();

    static {
        // K, V ==> ID, Position
        tagFieldPositions.put(10, new PioNerdPosition(0.00,10.00,0.00));
        tagFieldPositions.put(11, new PioNerdPosition(10.00,10.00,0.00));

    }

    public static double getAbsoluteX(int id) {return tagFieldPositions.get(id).getX();}
    public static double getAbsoluteY(int id) {return tagFieldPositions.get(id).getY();}
    public static double getAbsoluteZ(int id) {return tagFieldPositions.get(id).getZ();}
}
