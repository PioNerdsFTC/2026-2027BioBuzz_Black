package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Vision.Vision;

@TeleOp(name="VisionOpMode")
public class VisionOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {

        Vision.init(hardwareMap);

        waitForStart();
    }
}
