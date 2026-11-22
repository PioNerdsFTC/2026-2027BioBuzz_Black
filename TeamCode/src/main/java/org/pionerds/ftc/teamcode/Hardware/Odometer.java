package org.pionerds.ftc.teamcode.Hardware;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.sun.tools.doclint.Env;

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
        if (otos.selfTest()) {
            Environment.Capabilities.otos = true;
            offline = false;
            init(otos);
            return;
        }

        Scheduler.addTask(200, obj -> {
            verifyOtos();
        });
    }

    public void init(SparkFunOTOS otos) {
        this.otos = otos;

        otos.calibrateImu();

        otos.setLinearUnit(DistanceUnit.CM);
        otos.setAngularUnit(AngleUnit.RADIANS);

//        otos.setOffset(new SparkFunOTOS.Pose2D());

        otos.resetTracking();
    }

    /** Clears the cached pose so a new run cannot read the previous run's values before the next tick. */
    public void reset() {
        x = 0;
        y = 0;
        h = 0;
    }

    public void tick() {
        if (otos == null) return;

        check: if (!Environment.Capabilities.otos && !offline) {
            if (otos.selfTest()) {
                Environment.Capabilities.otos = true;
                init(otos);
                break check;
            }

            offline = true;

            Scheduler.addTask(500, obj -> otos.selfTest());
            return;
        }

        SparkFunOTOS.Pose2D pos = otos.getPosition();

        x = pos.x;
        y = pos.y;
        h = pos.h;
    }
}
