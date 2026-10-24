package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Motor and Servo Testing", group="Linear OpMode")
public class TestingIntakeMotorAndServo extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor intakeMotor = null;
    private Servo intakeServo = null;
    private DcMotor elevatorMotor = null;

    // Motor States
    private boolean motorIsSpinning = false;
    private boolean lastButtonState = false;

    // Servo States
    @Override
    public void runOpMode() {
        intakeMotor = hardwareMap.get(DcMotor.class, "IntakeMotor");
        intakeServo = hardwareMap.get(Servo.class, "IntakeServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "ElevatorMotor");

        intakeServo.setDirection(Servo.Direction.REVERSE);
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {

            // --- Motor Toggle Control (X Button) ---
            boolean currentButtonState = gamepad1.x;

            if (currentButtonState && !lastButtonState) {
                motorIsSpinning = !motorIsSpinning;
            }
            lastButtonState = currentButtonState;

            if (motorIsSpinning) {
                intakeMotor.setPower(-1.0);
                elevatorMotor.setPower(RobotConfig.ELEVATOR_POWER);
            } else {
                intakeMotor.setPower(0.0);
                elevatorMotor.setPower(0.0);
            }
            double currentServoPosition = 0.0;
            if (gamepad1.dpad_up) {
                currentServoPosition += 0.002;
            } else if (gamepad1.dpad_down) {
                currentServoPosition -= 0.002;
            }
            currentServoPosition = Math.max(0.0, Math.min(1.0, currentServoPosition));

            intakeServo.setPosition(currentServoPosition);

            // --- Telemetry ---
            telemetry.addData("Servo Position", intakeServo.getPosition());
            telemetry.addData("Motor Spinning", motorIsSpinning);
            telemetry.addData("Runtime", runtime.toString());
            telemetry.addData("Elevator Power", RobotConfig.ELEVATOR_POWER);
            telemetry.update();
        }
    }
}
