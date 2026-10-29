package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Odometer {
    SparkFunOTOS otos;

    public double x = 0;
    public double y = 0;
    public double h = 0;


    public void init(SparkFunOTOS otos) {
        this.otos = otos;

        otos.calibrateImu();

        otos.setLinearUnit(DistanceUnit.CM);
        otos.setAngularUnit(AngleUnit.RADIANS);
    }

    public void tick() {
        SparkFunOTOS.Pose2D pos = otos.getPosition();

        x = pos.x;
        y = pos.y;
        h = pos.h;
    }
}
