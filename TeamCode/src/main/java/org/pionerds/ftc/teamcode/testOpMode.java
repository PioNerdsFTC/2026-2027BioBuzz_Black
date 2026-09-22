
package org.pionerds.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Test - teleop")
public class testOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        waitForStart();

        while (opModeIsActive()) {
            sleep(10);
        }
    }
}
