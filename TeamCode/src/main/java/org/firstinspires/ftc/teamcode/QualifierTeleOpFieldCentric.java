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

    @Override
    public void init() {
        // Initialize Pedro Pathing follower and localization
        follower = Constants.create(hardwareMap);

        // Map configuration names to hardware ports
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");

        nectarFlywheelMotor = hardwareMap.get(DcMotorEx.class, "nectarFlywheelMotor");
        pollenFlywheelMotor = hardwareMap.get(DcMotorEx.class, "pollenFlywheelMotor");

        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");

        // Reverse servo orientation for mechanical compatibility
        flowerServo.setDirection(Servo.Direction.REVERSE);

        // Set resting servo positions on startup
        pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        flowerServo.setPosition(RobotConfig.FLOWER_SERVO_UP_POSITION);

        // Display initialization telemetry and verify stored coordinate states from auto
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Stored Auto End Pose", "X: %.2f, Y: %.2f, Heading: %.2f deg",
                OpModeStorage.autonomousEndPose.x(),
                OpModeStorage.autonomousEndPose.y(),
                Math.toDegrees(OpModeStorage.autonomousEndPose.heading())
        );
        telemetry.update();
    }

    @Override
    public void start() {
        // Apply the static transfer pose directly to the follower tracking map
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
        runtime.reset();

        telemetry.clearAll();
        telemetry.addLine("TeleOp Started with Autonomous Final Pose!");
        telemetry.update();
    }

    @Override
    public void loop() {
        // 1. FIELD ORIENTATION RESET (Preserves X and Y positioning)
        boolean currentOptionsButton = gamepad1.options;
        if (currentOptionsButton && !lastOptionsButton) {
            double currentX = follower.pose().x();
            double currentY = follower.pose().y();

            // Re-apply pose with 0 degrees straight forward field-wise
            Pose resetPose = new Pose(currentX, currentY, 0.0);
            follower.setPose(resetPose);
        }
        lastOptionsButton = currentOptionsButton;

        // 2. FIELD-CENTRIC DRIVE MECHANICS
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x,
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

        // 6. SHOOTER AUTOMATIC PRE-SPOOL CONTROL
        boolean driverWantsToScore = gamepad1.x || gamepad1.b;
        if (driverWantsToScore) {
            pollenFlywheelMotor.setVelocity(RobotConfig.POLLEN_FLYWHEEL_VELOCITY);
            nectarFlywheelMotor.setVelocity(RobotConfig.NECTAR_FLYWHEEL_VELOCITY);
        } else {
            pollenFlywheelMotor.setVelocity(RobotConfig.POLLEN_FLYWHEEL_VELOCITY * 0.25);
            nectarFlywheelMotor.setVelocity(RobotConfig.NECTAR_FLYWHEEL_VELOCITY * 0.25);
        }

        // Isolate positive speed values for strict target comparison checks
        double currentPollenSpeed = Math.abs(pollenFlywheelMotor.getVelocity());
        double currentNectarSpeed = Math.abs(nectarFlywheelMotor.getVelocity());
        double pollenFullTarget = Math.abs(RobotConfig.POLLEN_FLYWHEEL_VELOCITY);
        double nectarFullTarget = Math.abs(RobotConfig.NECTAR_FLYWHEEL_VELOCITY);

        // 7. STRICT POWER THRESHOLD CALCULATION
        double pollenRequiredThreshold = pollenFullTarget * RobotConfig.SHOOTER_FULL_POWER_THRESHOLD;
        boolean pollenReady = currentPollenSpeed >= pollenRequiredThreshold;

        double nectarRequiredThreshold = nectarFullTarget * RobotConfig.SHOOTER_FULL_POWER_THRESHOLD;
        boolean nectarReady = currentNectarSpeed >= nectarRequiredThreshold;

        // 8. POLLEN GATE CONTROL (Button X - Strictly locked to target velocity threshold)
        if (gamepad1.x && pollenReady) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
        } else {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }

        // 9. NECTAR GATE CONTROL (Button B - Strictly locked to target velocity threshold)
        if (gamepad1.b && nectarReady) {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION);
        } else {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        }

        // 10. DIAGNOSTIC DATA MONITORING
        telemetry.addData("Heading", Math.toDegrees(follower.pose().heading()));
        telemetry.addData("Pollen Clear", gamepad1.x && pollenReady);
        telemetry.addData("Nectar Clear", gamepad1.b && nectarReady);
        telemetry.addData("Pollen Vel", currentPollenSpeed + " / " + pollenRequiredThreshold);
        telemetry.addData("Nectar Vel", currentNectarSpeed + " / " + nectarRequiredThreshold);
        telemetry.addData("Runtime", runtime.toString());
        telemetry.update();
    }
}
