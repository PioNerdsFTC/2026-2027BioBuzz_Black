package org.pionerds.ftc.teamcode.Coordination;

public class PioNerdPosition {

    private double x;
    private double y;
    private double z;

    public PioNerdPosition(double x, double y, double z){
        this.x = x; this.y = y; this.z = z;
    }


    public double deltaX(PioNerdPosition pos2){
        return Math.abs(pos2.x - this.x);
    }

    public double deltaY(PioNerdPosition pos2){
        return Math.abs(pos2.y - this.y);
    }

    public double deltaZ(PioNerdPosition pos2){
        return Math.abs(pos2.z - this.z);
    }

    public double delta2D (PioNerdPosition pos2){
        return Math.sqrt(Math.pow(pos2.x-this.x,2) + Math.pow(pos2.y-this.y,2));
    }

    public double delta3D (PioNerdPosition pos2){
        return Math.sqrt(Math.pow(pos2.x-this.x,2) + Math.pow(pos2.y-this.y,2) + Math.pow(pos2.z-this.z,2));
    }






    // Setters and Getters ======================

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
