package org.pionerds.ftc.teamcode.Drivetrain;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Driver.Driver;
import org.pionerds.ftc.teamcode.Hardware.Hardware;

public class OmniDrivetrain {

    private Driver driver;
    private final DcMotorEx[] motors = {null, null, null, null}; //front right, front left, back left, back right
    private final double[] motorSpeed = {0.0, 0.0, 0.0, 0.0};



    static {



    }



    public void fieldCentricDrive(){
        double x = driver.leftStickX();
        double y = driver.leftStickY();
        double gyroAngle = Hardware.yaw;

        // Tolerance-Dead Zone
        if(Math.abs(x) < 0.2 && Math.abs(y) < 0.2){
            nonEmergencyStop();
            return;
        }

        double[] fieldCentricPositionRequest = getFieldCentricPositionRequest(x,y,gyroAngle);

        x = fieldCentricPositionRequest[0];
        y = fieldCentricPositionRequest[1];
        double magnitude = fieldCentricPositionRequest[2];


        motorSpeed[0] = (-x - y);
        motorSpeed[1] = (-x + y);
        motorSpeed[2] = (x + y);
        motorSpeed[3] = (x - y);

        motorSpeed[0] *= magnitude;
        motorSpeed[1] *= magnitude;
        motorSpeed[2] *= magnitude;
        motorSpeed[3] *= magnitude;

    }

    // Returns an array of [x,y] that the robot should be moving toward. Factoring in the robot's angle. Only God can recreate this method.
    public double[] getFieldCentricPositionRequest(double x, double y, double robotOrientation){
        x = -x;
        y = -y;

        double mag = Math.sqrt((Math.pow(x,2) + Math.pow(y,2)));

        double joystickAngle = Math.atan2(y,x);
        double finalAngle = joystickAngle + robotOrientation;

        x = Math.cos(finalAngle);
        y = Math.sin(finalAngle);

        return new double[]{-x, -y, mag};

    }

    public void robotCentricDrive(){
        double x = driver.leftStickX();
        double y = driver.leftStickY();

        // Tolerance-Dead Zone
        if(Math.abs(x) < 0.2 && Math.abs(y) < 0.2){
            nonEmergencyStop();
            return;
        }

        motorSpeed[0] = (-x - y);
        motorSpeed[1] = (-x + y);
        motorSpeed[2] = (x + y);
        motorSpeed[3] = (x - y);
    }



    public void updateMotors(){
        for (int i = 0; i < 4; i++) {
            motors[i].setPower(motorSpeed[i]);
        }
    }

    public void normalizeMotors(){
        // Don't judge me... >:(
        double max = (
                motorSpeed[0]>motorSpeed[1] && motorSpeed[0]>motorSpeed[2] && motorSpeed[0]>=motorSpeed[3] ? motorSpeed[0] :
                        (motorSpeed[1]>motorSpeed[0] && motorSpeed[1]>motorSpeed[2] && motorSpeed[1]>=motorSpeed[3] ? motorSpeed[1] :
                                (motorSpeed[2]>motorSpeed[0] && motorSpeed[2]>motorSpeed[1] && motorSpeed[2]>=motorSpeed[3] ? motorSpeed[2] :
                                        (motorSpeed[3]>motorSpeed[0] && motorSpeed[3]>motorSpeed[1] && motorSpeed[3]>=motorSpeed[2] ? motorSpeed[3] : 0.00)
                                )
                        )
                );

        motorSpeed[0] /= max;
        motorSpeed[1] /= max;
        motorSpeed[2] /= max;
        motorSpeed[3] /= max;
    }

    public void emergencyStop() {
        motorSpeed[0] = 0.00;
        motorSpeed[1] = 0.00;
        motorSpeed[2] = 0.00;
        motorSpeed[3] = 0.00;

        updateMotors();

        // Spawn new thread here to lock motors from being updated.
    }

    public void nonEmergencyStop() {
        motorSpeed[0] = 0.00;
        motorSpeed[1] = 0.00;
        motorSpeed[2] = 0.00;
        motorSpeed[3] = 0.00;

        updateMotors();
    }
}