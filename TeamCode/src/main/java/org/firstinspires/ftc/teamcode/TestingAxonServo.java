package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Motor and Servo Testing", group="Linear OpMode")
public class TestingAxonServo extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private ServoImplEx axonServo = null;

    @Override
    public void runOpMode() {
        axonServo = hardwareMap.get(ServoImplEx.class, "AxonServo");
        axonServo.setDirection(Servo.Direction.REVERSE);

        axonServo.setPwmRange(new PwmControl.PwmRange(500, 2500));

        double currentServoPosition = 0.5;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                currentServoPosition += 0.002;
            } else if (gamepad1.dpad_down) {
                currentServoPosition -= 0.002;
            }

            currentServoPosition = Math.max(0.0, Math.min(1.0, currentServoPosition));

            axonServo.setPosition(currentServoPosition);

            // --- Telemetry ---
            telemetry.addData("Servo Position", "%.3f", axonServo.getPosition());
            telemetry.addData("Runtime", runtime.toString());
            telemetry.addData("Elevator Power", RobotConfig.ELEVATOR_POWER);
            telemetry.update();
        }
    }
}
