package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedro.Constants;
@Disabled
@TeleOp(name = "Robot Qualifier TeleOp")
public class QualifierTeleOpRobotCentric extends OpMode {

    private Follower follower;
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor intakeMotor;
    private Servo flowerServo;
    private DcMotor elevatorMotor;
    private DcMotorEx pollenFlywheelMotor;
    private DcMotorEx nectarFlywheelMotor;
    private Servo pollenGateServo;
    private Servo nectarGateServo;

    // Toggle state variables for the intake motor (Left Bumper)
    private boolean motorIsSpinning = false;
    private boolean lastButtonState = false;

    // Toggle state variables for the flower servo (Left Trigger)
    private double currentServoPosition = RobotConfig.FLOWER_SERVO_UP_POSITION;
    private boolean lastTriggerState = false;

    // Toggle state variables for the shooter flywheels (Right Trigger)
    private boolean shooterIsSpinning = false;
    private boolean lastShooterTriggerState = false;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);

        // Hardware Retrievals
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");

        nectarFlywheelMotor = hardwareMap.get(DcMotorEx.class, "nectarFlywheelMotor");
        pollenFlywheelMotor = hardwareMap.get(DcMotorEx.class, "pollenFlywheelMotor");

        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");

        flowerServo.setDirection(Servo.Direction.REVERSE);

        // Initialize gates to closed positions immediately
        pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        flowerServo.setPosition(RobotConfig.FLOWER_SERVO_UP_POSITION);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
        runtime.reset();
    }

    @Override
    public void loop() {
        // 1. DRIVETRAIN (Pedro Pathing Robot-Centric)
        follower.manual(
                -gamepad1.left_stick_y, // Drive Forward/Backward
                -gamepad1.left_stick_x,  // Strafe Left/Right
                -gamepad1.right_stick_x  // Turn Left/Right
        );
        follower.update();

        // 2. TOGGLE LOGIC FOR INTAKE MOTOR (Gamepad 1 Left Bumper)
        boolean currentButtonState = gamepad1.left_bumper;
        if (currentButtonState && !lastButtonState) {
            motorIsSpinning = !motorIsSpinning;
        }
        lastButtonState = currentButtonState;

        // 3. INTAKE & ELEVATOR BEHAVIOR WITH REVERSE INTERLOCK (Right Bumper Override)
        if (gamepad1.right_bumper) {
            intakeMotor.setPower(-RobotConfig.INTAKE_POWER);
            elevatorMotor.setPower(-RobotConfig.ELEVATOR_POWER);
        } else if (motorIsSpinning) {
            intakeMotor.setPower(RobotConfig.INTAKE_POWER);
            elevatorMotor.setPower(RobotConfig.ELEVATOR_POWER);
        } else {
            intakeMotor.setPower(0.0);
            elevatorMotor.setPower(0.0);
        }

        // 4. SEQUENTIAL TOGGLE LOGIC FOR FLOWER SERVO (Left Trigger)
        boolean currentTriggerState = gamepad1.left_trigger > 0.5;
        if (currentTriggerState && !lastTriggerState) {
            if (currentServoPosition == RobotConfig.FLOWER_SERVO_DOWN_POSITION) {
                currentServoPosition = RobotConfig.FLOWER_SERVO_UP_POSITION;
            } else {
                currentServoPosition = RobotConfig.FLOWER_SERVO_DOWN_POSITION;
            }
        }
        lastTriggerState = currentTriggerState;

        flowerServo.setPosition(currentServoPosition);

        // 5. TOGGLE LOGIC FOR SHOOTER POWERS (Gamepad 1 Right Trigger)
        boolean currentShooterTriggerState = gamepad1.right_trigger > 0.5;
        if (currentShooterTriggerState && !lastShooterTriggerState) {
            shooterIsSpinning = !shooterIsSpinning;
        }
        lastShooterTriggerState = currentShooterTriggerState;

        // Uses configuration power options directly
        if (shooterIsSpinning) {
            pollenFlywheelMotor.setVelocity(RobotConfig.POLLEN_FLYWHEEL_VELOCITY);
            nectarFlywheelMotor.setVelocity(RobotConfig.NECTAR_FLYWHEEL_VELOCITY);
        } else {
            pollenFlywheelMotor.setPower(0.0);
            nectarFlywheelMotor.setPower(0.0);
        }

        if (gamepad1.x) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
        } else {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }

        if (gamepad1.b) {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION);
        } else {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        }

        // 8. TELEMETRY MONITORING
        telemetry.addData("Flower Servo Position", flowerServo.getPosition());
        telemetry.addData("Pollen Gate Holding Open", gamepad1.x);
        telemetry.addData("Nectar Gate Holding Open", gamepad1.b);
        telemetry.addData("Intake Forward Toggle Active", motorIsSpinning);
        telemetry.addData("Intake Reversing (Held)", gamepad1.right_bumper);
        telemetry.addData("Shooter Spinning", shooterIsSpinning);
        telemetry.addData("Pollen Shooter Actual Vel", pollenFlywheelMotor.getVelocity());
        telemetry.addData("Nectar Shooter Actual Vel", nectarFlywheelMotor.getVelocity());
        telemetry.addData("Runtime", runtime.toString());
        telemetry.update();
    }
}
