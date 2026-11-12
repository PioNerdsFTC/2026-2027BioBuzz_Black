package org.pionerds.ftc.teamcode.Autonomous;

import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Driver.PuppetDriver;
import org.pionerds.ftc.teamcode.Drivetrain.OmniDrivetrain;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

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

        Scheduler.addTask((obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.AUTO) return;
            if (!Scheduler.running) return;

            if (positionQueue.isEmpty()) {
                stop();
                return;
            }

            moveToPositionTick(positionQueue.get(0));
        });

        Scheduler.addTask("exit", (obj) -> {
            positionQueue.clear();
            stop();
        });
    }


    /**
     * Queue a movement to {x, y, tolerance} in Coordination units
     */
    public static void requestMovement(double[] xyT){
        positionQueue.add(xyT);
    }

    public static void moveToPositionTick(double[] position){
        cacheTarget(position);

        if(isWithinTolerance()){
            positionQueue.remove(position);
            stop();
            return;
        }

        puppet.setX(calculateSpeedForceX());
        puppet.setY(-calculateSpeedForceY());
    }

    private static void stop(){
        puppet.setX(0.00);
        puppet.setY(0.00);
    }


    private static void cacheTarget(double[] targetPos){
        x = Coordination.x;
        y = Coordination.y;

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
        return calculateSpeedForce(xf, x);
    }

    private static double calculateSpeedForceY(){
        return calculateSpeedForce(yf, y);
    }

    private static double calculateSpeedForce(double target, double current){
        double Δ = getΔ(target, current);
        double direction = Math.signum(target - current);

        if(Δ <= t) return 0.00;
        else if(Δ >= 20) return direction * maxSpeed;
        else if(Δ >= 10) return direction * mediumSpeed;
        else return direction * slowSpeed;
    }
}
