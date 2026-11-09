package org.pionerds.ftc.teamcode.Autonomous;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.Drivetrain;
import org.pionerds.ftc.teamcode.Drivetrain.OmniDrivetrain;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Hardware.Odometer;

import java.util.ArrayList;

public class Autonomous {

    private static double xf = 0.00;
    private static double yf = 0.00;
    private static double x = 0.00;
    private static double y = 0.00;
    private static double t = 0.00;

    private static double maxSpeed = 0.50;
    private static double mediumSpeed = 0.20;
    private static double slowSpeed = 0.05;
    private static PuppetDriver puppet = new PuppetDriver();

    private static ArrayList<double[]> positionQueue = new ArrayList<>();


    static{
        OmniDrivetrain.OVERRIDE_DRIVER(puppet);
    }


    public static void requestMovement(double[] xyT){
        positionQueue.add(xyT);
    }

    public static void moveToPositionTick(double[] position){
        cacheTarget(position);

        double[] driveForces = {0.00,0.00};

        // set the rightward-force
        driveForces[0] = calculateSpeedForceX();

        // set the forward-force
        driveForces[1] = calculateSpeedForceY();

        if(isWithinTolerance()) OmniDrivetrain.fieldCentricDrive();
    }


    private static void cacheTarget(double[] targetPos){
        x = Hardware.odometer.x;
        y = Hardware.odometer.y;

        xf = targetPos[0];
        yf = targetPos[1];
        t = targetPos[2];
    }

    private static boolean isWithinTolerance(){
        if(Math.abs(xf-x) <= t && Math.abs(yf-y) <= t) return true;
        return false;
    }

    private static double getΔ(double p2, double p1){
        return Math.abs(p2-p1);
    }

    private static double calculateSpeedForceX(){
        if(getΔ(xf,x) >= 20) return maxSpeed;
        else if(getΔ(xf,x) >= 10) return mediumSpeed;
        else return slowSpeed;
    }

    private static double calculateSpeedForceY(){
        if(getΔ(yf,y) >= 20) return maxSpeed;
        else if(getΔ(yf,y) >= 10) return mediumSpeed;
        else return slowSpeed;
    }


}
