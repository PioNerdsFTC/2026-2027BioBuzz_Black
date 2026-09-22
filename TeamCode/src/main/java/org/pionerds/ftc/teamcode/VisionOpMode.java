package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pionerds.ftc.teamcode.Coordination.Coordination;
import org.pionerds.ftc.teamcode.Vision.Vision;

@TeleOp(name="VisionOpMode")
public class VisionOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {

        Vision.init(hardwareMap);
//        Vision.addTelemetry(telemetry);
        Coordination.addTelemetry(telemetry);

        waitForStart();
        while(opModeIsActive()){
//            Vision.collectData();
            double disp = -10000;

            /*
            if(Vision.getTagFTCPose(21) != null){
                disp = Vision.getTagFTCPose(21).x;
            }
             */

            Coordination.cacheCoordinates(false, false);

            if(Coordination.getRobotPosition() != null){
                telemetry.addLine("robotCoordsX: "+ Coordination.getRobotPosition().getX());
            } else {
                telemetry.addLine("robot position is null.");
            }

            //telemetry.addLine("displace:" + disp);
            telemetry.update();



        }
    }
}
