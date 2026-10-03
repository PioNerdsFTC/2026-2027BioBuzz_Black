package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class DeprecatedSwerveDrivetrain {
    private int servoCount = 4;
    private DcMotorEx[] motors;
    private CRServoImplEx[] servos;
    private double[] servoAngles;


    public DeprecatedSwerveDrivetrain() {
        motors = new DcMotorEx[servoCount];
        servos = new CRServoImplEx[servoCount];
        for (int i = 0; i<servoCount; i++){
            servos[i].setDirection(DcMotorSimple.Direction.FORWARD);
            servoAngles[i] = 0.00;
        }
    }

    // Needs implementation once the IMUs are added to the robot
    private void fetchServoAngles(){

    }

    // Will be called every tick in the op-mode loop
    public void driveSwerve(double thetaT, double magnitude){
        for (int i = 0; i<servoCount; i++) {
//            rotateSwervoThetaTick(i,);
            rotateSwervoThetaTick(i, thetaT*getRevolutions(i), (isForwardFaster(thetaT,servoAngles[i]) ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE)); // PLEASE CHECK THIS
        }


    }

    // Physically checks the current rotation of the swervo and moves it accordingly
    private void rotateSwervoThetaTick(int servoIndex, double thetaAbs, DcMotorSimple.Direction direction){
        servos[servoIndex].setDirection(direction);

        double tolerance1 = Math.PI/8.00;
        double tolerance2 = Math.PI/24.00;

        double thetaS = servoAngles[servoIndex];
        double deltaTheta = thetaAbs-thetaS;

        if(deltaTheta > tolerance1) servos[servoIndex].setPower(1.00);
    }

    private boolean isForwardFaster(double thetaTarget, double thetaServo) {
        thetaTarget = normalizeAngle(thetaTarget);
        double thetaOppositeServo = normalizeAngle(thetaServo + Math.PI); // KEEP ABOVE NORMALIZE OVERRIDE OF THETA SERVO ANGLE
        thetaServo = normalizeAngle(thetaServo);

        return ((
                Math.abs( thetaServo - thetaTarget)
        ) <= (
                Math.abs( thetaOppositeServo - thetaTarget)
        ));
    }

    private double normalizeAngle(double theta){
        return ((theta + (2 * Math.PI)) % (2 * Math.PI));
    }

    private int getRevolutions(int servoIndex){
        return (int) (servoAngles[servoIndex] / 2 * Math.PI);
    }

}