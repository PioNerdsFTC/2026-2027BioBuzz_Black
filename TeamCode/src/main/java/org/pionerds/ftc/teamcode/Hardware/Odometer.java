package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Odometer {

    SparkFunOTOS otos;

    public double x = 0;
    public double y = 0;
    public double h = 0;

    public boolean offline = false;

    private void verifyOtos() {
        Scheduler.addTask(200, obj -> {
            verifyOtos();
        });

        if (otos.selfTest()) {
            Environment.Capabilities.otos = true;
            offline = false;
            init(otos);
            return;
        }

        Environment.Capabilities.otos = false;
        offline = true;
    }

    public void init(SparkFunOTOS otos) {
        this.otos = otos;

        otos.calibrateImu();

        otos.setLinearUnit(DistanceUnit.CM);
        otos.setAngularUnit(AngleUnit.RADIANS);

//        otos.setOffset(new SparkFunOTOS.Pose2D());

        otos.resetTracking();

        verifyOtos();
    }

    /** Clears the cached pose so a new run cannot read the previous run's values before the next tick. */
    public void reset() {
        x = 0;
        y = 0;
        h = 0;
    }

    public void tick() {
        if (otos == null) return;

        SparkFunOTOS.Pose2D pos = otos.getPosition();

        x = pos.x;
        y = pos.y;
        h = pos.h;
    }
}
