package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Gaming TeleOp", group = "Robot")
public class GamingTeleOp extends OpMode {
    private Follower follower;
    private DcMotor intakeMotor;
    private DcMotor elevatorMotor;
    private DcMotor pollenFlywheelMotor;
    private DcMotor nectarFlywheelMotor;
    private Servo pollenGateServo;
    private Servo nectarGateServo;
    private Servo flowerServo;
    private boolean flowerDown = false;
    private boolean previousB = false;
    private boolean gatesOpen = false;
    private boolean previousA = false;
    private boolean intakeOn = false;
    private boolean previousRT = false;
    private boolean elevatorOn = false;
    private boolean previousRB = false;
    private boolean flywheelsOn = false;
    private boolean previousY = false;

    @Override
    public void init() {
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeMotor.setPower(0.0);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");
        elevatorMotor.setPower(0.0);
        elevatorMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        pollenFlywheelMotor = hardwareMap.get(DcMotor.class, "pollenFlywheelMotor");
        pollenFlywheelMotor.setPower(0.0);
        pollenFlywheelMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        nectarFlywheelMotor = hardwareMap.get(DcMotor.class, "nectarFlywheelMotor");
        nectarFlywheelMotor.setPower(0.0);
        nectarFlywheelMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");
        gatesOpen = false;
        updateGatePositions();
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        flowerDown = false;
        flowerServo.setPosition(RobotConfig.FLOWER_SERVO_UP_POSITION);
        follower = Constants.create(hardwareMap);
        telemetry.addLine("Robot-centric: left stick to move, right stick to turn");
        telemetry.update();
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        // Pedro uses positive strafe left and positive turn counterclockwise.
        double strafe = -gamepad1.left_stick_x;
        double turn = -gamepad1.right_stick_x;

        follower.manual(forward, strafe, turn);
        follower.update();

        boolean currentB = gamepad1.b;
        if (currentB && !previousB) {
            flowerDown = !flowerDown;
            flowerServo.setPosition(flowerDown
                    ? RobotConfig.FLOWER_SERVO_DOWN_POSITION
                    : RobotConfig.FLOWER_SERVO_UP_POSITION);
        }
        previousB = currentB;

        boolean currentA = gamepad1.a;
        if (currentA && !previousA) {
            gatesOpen = !gatesOpen;
            updateGatePositions();
        }
        previousA = currentA;

        boolean currentRT = gamepad1.right_trigger > 0.5;
        if (currentRT && !previousRT) {
            intakeOn = !intakeOn;
        }
        previousRT = currentRT;
        intakeMotor.setPower(intakeOn ? RobotConfig.INTAKE_POWER : 0.0);

        boolean currentRB = gamepad1.right_bumper;
        if (currentRB && !previousRB) {
            elevatorOn = !elevatorOn;
        }
        previousRB = currentRB;
        elevatorMotor.setPower(elevatorOn ? RobotConfig.ELEVATOR_POWER : 0.0);

        boolean currentY = gamepad1.y;
        if (currentY && !previousY) {
            flywheelsOn = !flywheelsOn;
        }
        previousY = currentY;
        pollenFlywheelMotor.setPower(flywheelsOn ? RobotConfig.POLLEN_FLYWHEEL_POWER : 0.0);
        nectarFlywheelMotor.setPower(flywheelsOn ? RobotConfig.NECTAR_FLYWHEEL_POWER : 0.0);

        telemetry.addData("Drive Mode", "Robot-centric");
        telemetry.addData("Forward", forward);
        telemetry.addData("Strafe Left", strafe);
        telemetry.addData("Turn CCW", turn);
        telemetry.addData("Gates", gatesOpen ? "Open" : "Closed");
        telemetry.addData("pollenGateServo Position", pollenGateServo.getPosition());
        telemetry.addData("nectarGateServo Position", nectarGateServo.getPosition());
        telemetry.addData("flowerServo Position", flowerServo.getPosition());
        telemetry.addData("Intake On", intakeOn);
        telemetry.addData("Intake Power", intakeMotor.getPower());
        telemetry.addData("Elevator On", elevatorOn);
        telemetry.addData("Elevator Power", elevatorMotor.getPower());
        telemetry.addData("Flywheels On", flywheelsOn);
        telemetry.addData("Pollen Flywheel Power", pollenFlywheelMotor.getPower());
        telemetry.addData("Nectar Flywheel Power", nectarFlywheelMotor.getPower());
        telemetry.update();
    }

    private void updateGatePositions() {
        pollenGateServo.setPosition(gatesOpen
                ? RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION
                : RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        nectarGateServo.setPosition(gatesOpen
                ? RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION
                : RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
    }

    @Override
    public void stop() {
        intakeMotor.setPower(0.0);
        elevatorMotor.setPower(0.0);
        pollenFlywheelMotor.setPower(0.0);
        nectarFlywheelMotor.setPower(0.0);
        follower.stop();
        follower.drivetrain.stop();
    }
}
