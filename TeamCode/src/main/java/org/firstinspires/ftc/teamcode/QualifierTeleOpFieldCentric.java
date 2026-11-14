package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Field Qualifier TeleOp")
public class QualifierTeleOpFieldCentric extends OpMode {

    // Subsystem hardware declarations
    private Follower follower;
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor intakeMotor;
    private Servo flowerServo;
    private DcMotor elevatorMotor;
    private DcMotorEx pollenFlywheelMotor;
    private DcMotorEx nectarFlywheelMotor;
    private Servo pollenGateServo;
    private Servo nectarGateServo;

    // Intake state tracking variables
    private boolean motorIsSpinning = false;
    private boolean lastButtonState = false;

    // Flower servo tracking state
    private double currentServoPosition = RobotConfig.FLOWER_SERVO_UP_POSITION;
    private boolean lastTriggerState = false;

    // Heading reset tracking state variable
    private boolean lastOptionsButton = false;

    // Manual targets initialized with your exact RobotConfig velocity constants
    private double pollenManualVelocity = RobotConfig.POLLEN_FLYWHEEL_VELOCITY;
    private double nectarManualVelocity = RobotConfig.NECTAR_FLYWHEEL_VELOCITY;

    @Override
    public void init() {
        // Initialize Pedro Pathing follower and localization
        follower = Constants.create(hardwareMap);

        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");

        nectarFlywheelMotor = hardwareMap.get(DcMotorEx.class, "nectarFlywheelMotor");
        pollenFlywheelMotor = hardwareMap.get(DcMotorEx.class, "pollenFlywheelMotor");

        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");

        // Configure flywheels for closed-loop velocity encoder modes
        nectarFlywheelMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        nectarFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        pollenFlywheelMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        pollenFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        flowerServo.setDirection(Servo.Direction.REVERSE);

        // Snap servos to your configuration resting points on initialization
        pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        flowerServo.setPosition(RobotConfig.FLOWER_SERVO_UP_POSITION);

        // Reset runtime dynamic values to match configuration on init
        pollenManualVelocity = RobotConfig.POLLEN_FLYWHEEL_VELOCITY;
        nectarManualVelocity = RobotConfig.NECTAR_FLYWHEEL_VELOCITY;

        telemetry.addData("Status", "Initialized Cleanly!");
        telemetry.update();
    }

    @Override
    public void start() {
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
        runtime.reset();
    }

    @Override
    public void loop() {
        // 1. FIELD ORIENTATION RESET (Gamepad 1 Options)
        boolean currentOptionsButton = gamepad1.options;
        if (currentOptionsButton && !lastOptionsButton) {
            Pose resetPose = new Pose(follower.pose().x(), follower.pose().y(), 0.0);
            follower.setPose(resetPose);
        }
        lastOptionsButton = currentOptionsButton;

        // 2. FIELD-CENTRIC DRIVE MECHANICS
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                follower.pose().heading()
        );
        follower.manual(powers);
        follower.update();

        // 3. INTAKE MOTOR TOGGLE (Left Bumper rising-edge)
        boolean currentButtonState = gamepad1.left_bumper;
        if (currentButtonState && !lastButtonState) {
            motorIsSpinning = !motorIsSpinning;
        }
        lastButtonState = currentButtonState;

        // 4. INTAKE & ELEVATOR RUN LOGIC (Right Bumper Override)
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

        // 5. FLOWER SERVO TOGGLE (Left Trigger sequential snap)
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

        // 6. GAMEPAD 2 MANUALLY ADJUST VELOCITY CHANNELS
        if (gamepad2.left_trigger > 0.5) {
            pollenManualVelocity += 10.0;
        } else if (gamepad2.left_bumper) {
            pollenManualVelocity -= 10.0;
        }

        if (gamepad2.right_trigger > 0.5) {
            // Respects your configuration direction mapping (+10 moves away from 0 toward -2100)
            nectarManualVelocity -= 10.0;
        } else if (gamepad2.right_bumper) {
            nectarManualVelocity += 10.0;
        }

        // 7. SHOOTER AUTOMATIC PRE-SPOOL CONTROL
        boolean driverWantsToScore = gamepad1.x || gamepad1.b;
        if (driverWantsToScore) {
            pollenFlywheelMotor.setVelocity(pollenManualVelocity);
            nectarFlywheelMotor.setVelocity(nectarManualVelocity);
        } else {
            pollenFlywheelMotor.setVelocity(pollenManualVelocity * 0.75);
            nectarFlywheelMotor.setVelocity(nectarManualVelocity * 0.75);
        }

        // Isolate absolute speed values for comparison checks
        double currentPollenSpeed = Math.abs(pollenFlywheelMotor.getVelocity());
        double currentNectarSpeed = Math.abs(nectarFlywheelMotor.getVelocity());
        double pollenFullTarget = Math.abs(pollenManualVelocity);
        double nectarFullTarget = Math.abs(nectarManualVelocity);

        // Threshold targets update dynamically as Gamepad 2 modifies targets
        double pollenRequiredThreshold = pollenFullTarget * RobotConfig.SHOOTER_FULL_POWER_THRESHOLD;
        boolean pollenReady = currentPollenSpeed >= pollenRequiredThreshold;

        double nectarRequiredThreshold = nectarFullTarget * RobotConfig.SHOOTER_FULL_POWER_THRESHOLD;
        boolean nectarReady = currentNectarSpeed >= nectarRequiredThreshold;

        // 8. POLLEN GATE CONTROL (Button X)
        if (gamepad1.x && pollenReady) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
        } else {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }

        // 9. NECTAR GATE CONTROL (Button B)
        if (gamepad1.b && nectarReady) {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION);
        } else {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        }

        // 10. DIAGNOSTIC DATA MONITORING
        telemetry.addData("Heading", Math.toDegrees(follower.pose().heading()));
        telemetry.addData("Pollen Target", pollenManualVelocity);
        telemetry.addData("Nectar Target", nectarManualVelocity);
        telemetry.addData("Pollen Vel", currentPollenSpeed + " / " + pollenRequiredThreshold);
        telemetry.addData("Nectar Vel", currentNectarSpeed + " / " + nectarRequiredThreshold);
        telemetry.addData("Runtime", runtime.toString());
        telemetry.update();
    }
}
