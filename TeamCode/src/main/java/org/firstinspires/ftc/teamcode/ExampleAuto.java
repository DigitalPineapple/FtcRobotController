package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "Robot: Example Auto", group = "Robot")
public class ExampleAuto extends OpMode {

    private Follower follower;


    private DcMotor intakeMotor;
    private DcMotor elevatorMotor;
    private DcMotor pollenFlywheelMotor;
    private DcMotor nectarFlywheelMotor;

    private Servo flowerServo;
    private Servo pollenGateServo;
    private Servo nectorGateServo;
    private static final double FLOWER_HOME = 0.0;
    private static final double POLLEN_GATE_CLOSED = 0.0;
    private static final double POLLEN_GATE_OPEN = 1.0;
    private static final double NECTOR_GATE_CLOSED = 0.0;
    private static final double NECTOR_GATE_OPEN = 1.0;


    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startPose = poseFactory.of(0, 0, 0);
    private final Pose scorePose = poseFactory.of(40, 0, 90);
    private final Pose midPosePoint = poseFactory.of(10, 0, 90);
    private final Pose parkPose = poseFactory.of(0, 46, 90);
    private static final double SHOOTER_POWER = 1.0;
    private static final long SHOOT_TIME_MS = 2000;
    private static final double POSE_TOLERANCE = 2.0;

    private static final int DRIVE_TO_SCORE = 0;
    private static final int SHOOT = 1;
    private static final int DRIVE_TO_MID = 2;
    private static final int DRIVE_TO_PARK = 3;
    private static final int FINISHED = 4;

    private int autoState = DRIVE_TO_SCORE;
    private long shootStartTime;

    private Path startToScore() {
        return line(startPose, scorePose)
                .linear(startPose, scorePose);
    }

    private Path midPath() {
        return line(scorePose, midPosePoint)
                .linear(scorePose, midPosePoint);
    }

    private Path park() {
        return line(midPosePoint, parkPose)
                .linear(midPosePoint, parkPose);
    }


    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);


// Expansion Hub motors
        intakeMotor = hardwareMap.get(
                DcMotor.class, "intakeMotor");
        elevatorMotor = hardwareMap.get(
                DcMotor.class, "elevatorMotor");
        pollenFlywheelMotor = hardwareMap.get(
                DcMotor.class, "pollenFlywheelMotor");
        nectarFlywheelMotor = hardwareMap.get(
                DcMotor.class, "nectarFlywheelMotor");


// Servos
        flowerServo = hardwareMap.get(
                Servo.class, "flowerServo");
        pollenGateServo = hardwareMap.get(
                Servo.class, "pollenGateServo");
        nectorGateServo = hardwareMap.get(
                Servo.class, "nectarGateServo");


        intakeMotor.setPower(0);
        elevatorMotor.setPower(0);
        pollenFlywheelMotor.setPower(0);
        nectarFlywheelMotor.setPower(0);


        flowerServo.setPosition(FLOWER_HOME);
        pollenGateServo.setPosition(POLLEN_GATE_CLOSED);
        nectorGateServo.setPosition(NECTOR_GATE_CLOSED);

        telemetry.addLine("Autonomous Ready");
        telemetry.addLine("Drive -> Shoot -> Mid -> Park");
        telemetry.update();
    }


    @Override
    public void start() {
        autoState = DRIVE_TO_SCORE;
        schedule(follow(follower, startToScore()));
    }


    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();

        double x = follower.pose().x();
        double y = follower.pose().y();

        double distanceToScore = Math.hypot(
                x - scorePose.x(), y - scorePose.y());

        double distanceToMid = Math.hypot(
                x - midPosePoint.x(), y - midPosePoint.y());

        double distanceToPark = Math.hypot(
                x - parkPose.x(), y - parkPose.y());

        switch (autoState) {
            case DRIVE_TO_SCORE:
                if (distanceToScore < POSE_TOLERANCE) {
                    autoState = SHOOT;
                    shootStartTime = System.currentTimeMillis();

                    pollenFlywheelMotor.setPower(SHOOTER_POWER);
                    pollenGateServo.setPosition(POLLEN_GATE_OPEN);
                }
                break;

            case SHOOT:
                if (System.currentTimeMillis() -
                        shootStartTime
                        >= SHOOT_TIME_MS) {

                    pollenGateServo.setPosition(POLLEN_GATE_CLOSED);
                    pollenFlywheelMotor.setPower(1.0);

                    schedule(follow(follower, midPath()));
                    autoState = DRIVE_TO_MID;
                }
                break;

            case DRIVE_TO_MID:

                if (distanceToMid < POSE_TOLERANCE) {
                    schedule(follow(follower, park()));
                    autoState = DRIVE_TO_PARK;
                }
                break;

            case DRIVE_TO_PARK:

// CONFIGURE: Prefer confirmed Pedro path-completion detection.
                if (distanceToPark < POSE_TOLERANCE) {
                    autoState = FINISHED;
                }
                break;

            case FINISHED:
                pollenFlywheelMotor.setPower(0);
                pollenGateServo.setPosition(POLLEN_GATE_CLOSED);
                break;
        }

        telemetry.addData("Auto State", autoState);
        telemetry.addData("X", x);
        telemetry.addData("Y", y);
        telemetry.addData(
                "Heading",
                Math.toDegrees(follower.pose().heading()));
        telemetry.addData(
                "Pollen Flywheel",
                pollenFlywheelMotor.getPower());
        telemetry.update();
    }


    @Override
    public void stop() {
        if (intakeMotor != null) intakeMotor.setPower(0);
        if (elevatorMotor != null) elevatorMotor.setPower(0);
        if (pollenFlywheelMotor != null) pollenFlywheelMotor.setPower(0);
        if (nectarFlywheelMotor != null) nectarFlywheelMotor.setPower(0);

        if (pollenGateServo != null) {
            pollenGateServo.setPosition(POLLEN_GATE_CLOSED);
        }
        if (nectorGateServo != null) {
            nectorGateServo.setPosition(NECTOR_GATE_CLOSED);
        }
    }
}