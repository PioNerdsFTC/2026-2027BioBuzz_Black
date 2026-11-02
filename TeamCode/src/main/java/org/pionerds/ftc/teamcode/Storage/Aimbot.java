package org.pionerds.ftc.teamcode.Storage;

public class Aimbot {

    private static final double g = 9.80665;

    public static double getCalculatedAngle(double xi, double xf, double yf, double v){
        double[] experimentalAngles = calculateLaunchAnglesTheoretical(xi, xf, yf, v);

        double minAngle = experimentalAngles[0];
        double maxAngle = experimentalAngles[1];

        double averageAngle = (minAngle+maxAngle)/2.0;

        return empericalCorrection(averageAngle);
    }

    private static double empericalCorrection(double angle){
        return angle + 0.00;
    }

    public static double[] calculateLaunchAnglesTheoretical(double xi, double xf, double yf, double v){
        double Δx = xf - xi;
        double Δy = yf;

        double ΔxSq = Δx * Δx;
        double vv = v * v;
        double vvvv = vv * vv;

        // Discriminant / inner term under the square root: v^4 - g * (g * dx^2 + 2 * dy * v^2)
        double innerSqrt = vvvv - g * (g * ΔxSq + 2 * Δy * vv);

        if (innerSqrt < 0) {
            throw new IllegalArgumentException("Target is out of range for the given launch velocity!");
        }

        double sqrtTerm = Math.sqrt(innerSqrt);
        double denominator = g * Δx;

        // min angle
        double tanMin = (vv - sqrtTerm) / denominator;

        // max angle
        double tanMax = (vv + sqrtTerm) / denominator;

        double thetaMinRad = Math.atan(tanMin);
        double thetaMaxRad = Math.atan(tanMax);

        return new double[]{
                thetaMinRad,
                thetaMaxRad,
        };
    }
}
