
package org.firstinspires.ftc.teamcode;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "Pollen Shooting Auto Qual")
public class PollenShootingAutoQual extends OpMode {

    private Follower follower;
    private DcMotorEx pollenFlywheelMotor;
    private Servo pollenGateServo;
    private final ElapsedTime shootingTimer = new ElapsedTime();
    private static final double SHOOTING_DURATION_SECONDS = 7.0;
    private static final double SHAKE_START_SECONDS = 3.0;
    private static final double SHAKE_RETURN_SECONDS = 5.5;
    private static final double SHAKE_AMPLITUDE_INCHES = 4.0;
    private static final int SHAKE_CYCLES = 2;

    private enum ShootingState { IDLE, SPINNING_UP, SHOOTING, DRIVING }
    private ShootingState shootingState = ShootingState.IDLE;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    // Poses
    private final Pose startPose = poseFactory.of(48, 0, 270);
    private final Pose midPose = poseFactory.of(10, 36, 270);
    private final Pose finalPose = poseFactory.of(4, 84, 270);

    // Path methods
    private Path startToMid() {
        return line(startPose, midPose).linear(startPose, midPose);
    }

    private Path midToFinal() {
        return line(midPose, finalPose).linear(midPose, finalPose);
    }

    // Autonomous routine
    private Command autoRoutine() {
        return sequential(
                follow(follower, startToMid()),
                follow(follower, midToFinal())
        );
    }

    @Override
    public void init() {
        Scheduler.reset();

        pollenFlywheelMotor = hardwareMap.get(DcMotorEx.class, "pollenFlywheelMotor");
        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        stopShooter();
        shootingState = ShootingState.IDLE;

        follower = Constants.create(hardwareMap);

        follower.setPose(startPose);
        follower.update();
    }

    @Override
    public void start() {
        pollenFlywheelMotor.setVelocity(RobotConfig.POLLEN_FLYWHEEL_VELOCITY);
        shootingState = ShootingState.SPINNING_UP;
    }

    @Override
    public void loop() {
        follower.update();

        if (shootingState == ShootingState.SPINNING_UP) {
            double requiredSpeed = Math.abs(RobotConfig.POLLEN_FLYWHEEL_VELOCITY)
                    * RobotConfig.SHOOTER_FULL_POWER_THRESHOLD;
            if (Math.abs(pollenFlywheelMotor.getVelocity()) >= requiredSpeed) {
                pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
                shootingTimer.reset();
                shootingState = ShootingState.SHOOTING;
            }
        } else if (shootingState == ShootingState.SHOOTING) {
            double shootingSeconds = shootingTimer.seconds();
            if (shootingSeconds >= SHOOTING_DURATION_SECONDS) {
                stopShooter();
                shootingState = ShootingState.DRIVING;
                schedule(autoRoutine());
            } else if (shootingSeconds >= SHAKE_START_SECONDS) {
                // Two larger X-axis oscillations, then hold home to settle by six seconds.
                double progress = Math.min(1.0, (shootingSeconds - SHAKE_START_SECONDS)
                        / (SHAKE_RETURN_SECONDS - SHAKE_START_SECONDS));
                double xOffset = progress < 1.0
                        ? SHAKE_AMPLITUDE_INCHES * Math.sin(2.0 * Math.PI * SHAKE_CYCLES * progress)
                        : 0.0;
                follower.hold(poseFactory.of(startPose.x() + xOffset, startPose.y(), 270));
            }
        }

        Scheduler.execute();

        telemetry.addData("Shooting State", shootingState);
        telemetry.addData("Pollen Velocity", pollenFlywheelMotor.getVelocity());
        telemetry.addData("X", follower.pose().x());
        telemetry.addData("Y", follower.pose().y());
        telemetry.addData(
                "Heading",
                Math.toDegrees(follower.pose().heading())
        );
        telemetry.addData("Follower Mode", follower.mode());

        telemetry.update();
    }

    private void stopShooter() {
        if (pollenGateServo != null) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }
        if (pollenFlywheelMotor != null) {
            pollenFlywheelMotor.setVelocity(0);
            pollenFlywheelMotor.setPower(0);
        }
    }

    @Override
    public void stop() {
        stopShooter();
        Scheduler.reset();

    // Automatically grabs and saves the final coordinates when the match ends
        if (follower != null) {
            OpModeStorage.autonomousEndPose = follower.pose();
        }
    }
}

