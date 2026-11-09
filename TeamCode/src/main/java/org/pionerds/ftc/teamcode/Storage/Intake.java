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
            intake1 = Hardware.mapping.getMotor("intake0");
        });

        Scheduler.addTask("trigger-intake:down", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            intake0.setPower(1);
            intake1.setPower(1);
        });
        Scheduler.addTask("trigger-intake:up", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            intake0.setPower(0);
            intake1.setPower(0);
        });


        Scheduler.addTask("trigger-ejection-intake:down", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            intake0.setPower(-1);
            intake1.setPower(-1);
        });
        Scheduler.addTask("trigger-intake:up", (obj) -> {
            if (Environment.Parameters.operatingEnvironment != Environment.Parameters.OperatingEnvironment.TELE) return;

            intake0.setPower(0);
            intake1.setPower(0);
        });
    }
}
