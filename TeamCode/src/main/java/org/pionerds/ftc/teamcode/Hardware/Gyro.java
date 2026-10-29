package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.pionerds.ftc.teamcode.Logging.Logger;

public class Gyro {

    private IMU gyro = null;
    private IMU.Parameters params;

    public void init() {
        gyro = Hardware.mapping.getIMU();

        if (gyro == null) {
            Logger.error("IMU not found, gyro disabled");
            return;
        }

        params = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.FORWARD,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
        );
        gyro.initialize(params);
        gyro.resetYaw();

        Logger.log("Gyro Initialized");
    }

    /**
     * Resets the yaw angle of the gyro to zero.
     */
    public void resetYaw() {
        if (gyro != null) {
            gyro.resetYaw();
        }
    }

    /**
     * Gets the accumulated angles (yaw, pitch, roll) by adding stored angles to current gyro readings.
     * The returned array contains [yaw, pitch, roll] in the gyro's default angle unit (usually degrees).
     *
     * @return double array containing [yaw, pitch, roll]
     */
    public double[] getAngles() {
        double[] result = { 0.0, 0.0, 0.0 };

        if (gyro == null) return result;

        YawPitchRollAngles newGyroData = gyro.getRobotYawPitchRollAngles();
//        double[] oldGyroData = DataStorage.getAllStoredAngles();

        result[0] = /* oldGyroData[0] + */ newGyroData.getYaw(AngleUnit.RADIANS);
        result[1] = /* oldGyroData[1] + */ newGyroData.getPitch(AngleUnit.RADIANS);
        result[2] = /* oldGyroData[2] + */ newGyroData.getRoll(AngleUnit.RADIANS);

        return result;
    }
}