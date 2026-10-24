package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Field Scrimmage TeleOp")
public class ScrimmageTeleOpFieldCentric extends OpMode {

    private Follower follower;
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor intakeMotor;
    private Servo flowerServo;
    private DcMotor elevatorMotor;
    private DcMotor pollenFlywheelMotor;
    private DcMotor nectarFlywheelMotor;
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

    // Toggle state variables for Pollen Gate (Button X)
    private boolean pollengateIsOpen = false;
    private boolean lastXButtonState = false;

    // Toggle state variables for Nectar Gate (Button B)
    private boolean nectargateIsOpen = false;
    private boolean lastBButtonState = false;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);

        // Hardware Retrievals
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");

        nectarFlywheelMotor = hardwareMap.get(DcMotor.class, "nectarFlywheelMotor");
        pollenFlywheelMotor = hardwareMap.get(DcMotor.class, "pollenFlywheelMotor");

        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");

        flowerServo.setDirection(Servo.Direction.REVERSE);

        // Intialize gates to closed positions immediately
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
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad1.left_stick_x,
                gamepad1.left_stick_y,
                gamepad1.right_stick_x,
                follower.pose().heading()
        );
        follower.manual(powers);
        follower.update();

        // 2. TOGGLE LOGIC FOR INTAKE MOTOR (Gamepad 1 Left Bumper)
        boolean currentButtonState = gamepad1.left_bumper;
        if (currentButtonState && !lastButtonState) {
            motorIsSpinning = !motorIsSpinning;
        }
        lastButtonState = currentButtonState;

        // 3. INTAKE & ELEVATOR BEHAVIOR
        if (motorIsSpinning) {
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
            pollenFlywheelMotor.setPower(RobotConfig.POLLEN_FLYWHEEL_POWER);
            nectarFlywheelMotor.setPower(RobotConfig.NECTAR_FLYWHEEL_POWER);
        } else {
            pollenFlywheelMotor.setPower(0.0);
            nectarFlywheelMotor.setPower(0.0);
        }

        // 6. TOGGLE LOGIC FOR POLLEN GATE (Button X)
        boolean currentXState = gamepad1.x;
        if (currentXState && !lastXButtonState) {
            pollengateIsOpen = !pollengateIsOpen;
        }
        lastXButtonState = currentXState;

        if (pollengateIsOpen) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
        } else {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }

        // 7. TOGGLE LOGIC FOR NECTAR GATE (Button B)
        boolean currentBState = gamepad1.b;
        if (currentBState && !lastBButtonState) {
            nectargateIsOpen = !nectargateIsOpen;
        }
        lastBButtonState = currentBState;

        if (nectargateIsOpen) {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION);
        } else {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        }

        // 8. TELEMETRY MONITORING
        telemetry.addData("Flower Servo Position", flowerServo.getPosition());
        telemetry.addData("Pollen Gate Open", pollengateIsOpen);
        telemetry.addData("Nectar Gate Open", nectargateIsOpen);
        telemetry.addData("Intake Spinning", motorIsSpinning);
        telemetry.addData("Shooter Spinning", shooterIsSpinning);
        telemetry.addData("Runtime", runtime.toString());
        telemetry.update();
    }
}
