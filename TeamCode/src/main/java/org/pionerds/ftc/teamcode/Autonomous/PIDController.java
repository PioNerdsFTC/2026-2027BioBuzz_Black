package org.pionerds.ftc.teamcode.Autonomous;

public class PIDController {

    public double kP;
    public double kI;
    public double kD;
    public double kS;

    public double maxOutput = 1.0;
    public double integralLimit = 0.25;
    public double integralZone = Double.POSITIVE_INFINITY;
    public double deadzone = 0;
    public double maxRatePerSec = Double.POSITIVE_INFINITY;
    public double derivativeAlpha = 0.5;

    private double integral = 0;
    private double lastError = 0;
    private double derivative = 0;
    private double lastOutput = 0;
    private boolean fresh = true;

    public PIDController(double kP, double kI, double kD, double kS) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kS = kS;
    }

    public void reset() {
        integral = 0;
        lastError = 0;
        derivative = 0;
        lastOutput = 0;
        fresh = true;
    }

    /**
     * @param error target minus measurement
     * @param dtSec seconds since the last update
     * @return controller output, in the same units as {@link #maxOutput}
     */
    public double update(double error, double dtSec) {
        if (!(dtSec > 0)) dtSec = 1e-3;

        if (Math.abs(error) <= deadzone) {
            integral = 0;
            lastError = error;
            fresh = false;
            lastOutput = 0;
            return 0;
        }

        if (Math.abs(error) <= integralZone) {
            integral += error * dtSec;
            if (kI != 0) {
                double limit = integralLimit / Math.abs(kI);
                integral = clamp(integral, -limit, limit);
            }
        } else {
            integral = 0;
        }

        if (fresh) {
            derivative = 0;
            fresh = false;
        } else {
            double raw = (error - lastError) / dtSec;
            derivative += derivativeAlpha * (raw - derivative);
        }
        lastError = error;

        double output = kP * error + kI * integral + kD * derivative;
        if (output != 0) output += Math.signum(output) * kS;
        output = clamp(output, -maxOutput, maxOutput);

        double maxStep = maxRatePerSec * dtSec;
        boolean growing = Math.abs(output) > Math.abs(lastOutput) || Math.signum(output) != Math.signum(lastOutput);
        if (growing && Math.abs(output - lastOutput) > maxStep) {
            output = lastOutput + Math.signum(output - lastOutput) * maxStep;
        }

        lastOutput = output;
        return output;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
