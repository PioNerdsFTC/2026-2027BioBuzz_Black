package org.pionerds.ftc.teamcode.Storage;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.pionerds.ftc.teamcode.Environment;
import org.pionerds.ftc.teamcode.Hardware.Hardware;
import org.pionerds.ftc.teamcode.Orchestration.Scheduler;

public class Intake {

    public static DcMotorEx intake0;
    public static DcMotorEx intake1;

    static {
        Scheduler.addTask("init", (obj) -> {
            intake0 = Hardware.mapping.getMotor("intake0");
            intake1 = Hardware.mapping.getMotor("intake1");
        });

        Scheduler.addTask("trigger-intake:down", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            setPower(1);
        });
        Scheduler.addTask("trigger-intake:up", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            setPower(0);
        });


        Scheduler.addTask("trigger-ejection-intake:down", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            setPower(-1);
        });
        Scheduler.addTask("trigger-ejection-intake:up", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            setPower(0);
        });
    }

    private static void setPower(double power) {
        if (intake0 != null) intake0.setPower(power);
        if (intake1 != null) intake1.setPower(power);
    }
}
