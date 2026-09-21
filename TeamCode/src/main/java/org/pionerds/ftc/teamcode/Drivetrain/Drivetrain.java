package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

public class Drivetrain {
    private DcMotorEx[] motors;
    private CRServoImplEx[] servos;


    public Drivetrain() {
        motors = new DcMotorEx[4];
        servos = new CRServoImplEx[4];
        for (CRServoImplEx servo : servos){
            servo.setDirection(DcMotorSimple.Direction.FORWARD);
        }
    }

    public void driveSwerve(double theta, double magnitude){



    }

    private void rotateDriveWheelsAbs(){

    }
    public void rotateDriveWheelsOffset(double theta){
        for (CRServoImplEx servo : servos){
            // update direction
            if(isForwardFaster(getWheelRotation(),theta))

            // check distance
            if( Math.abs(getWheelRotation()-theta) > 10.00 ) servo.setPower(1.00);
            if( Math.abs(getWheelRotation()-theta) > 3.00 ) servo.setPower(0.20);
            if( Math.abs(getWheelRotation()-theta) > 0.50 ) servo.setPower(0.00);
        }
    }

    private boolean isForwardFaster(double current, double target) {
        if (Math.abs(target - current) > Math.abs(180 - target - current)) return false;
        else return true;
    }
    // Not yet implemented. No idea how we're going to accomplish this..
    private double getWheelRotation(){
        return 0.00;
    }

    private void updateServoDirection(DcMotorSimple.Direction direction){
        for (CRServoImplEx servo : servos){
            servo.setDirection(direction);
        }
    }



}