
package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import static com.pedropathing.api.Paths.line;

@Autonomous(name = "Example Auto")
public class ExampleAuto extends OpMode {

    private Follower follower;
    private int step = 0;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose startPose = poseFactory.of(0, 0, 0);
    private final Pose scorePose = poseFactory.of(40, 0, 90);
    private final Pose parkPose = poseFactory.of(0, -5,180);

    private Path startToScore() {
        return line(startPose, scorePose)
                .linear(startPose, scorePose);
    }

    private Path park() {
        return line(scorePose, parkPose)
                .linear(scorePose, parkPose);
    }

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);
        follower.update();

        telemetry.addLine("Autonomous Ready");
        telemetry.update();
    }

    @Override
    public void start() {
        step = 0;
        follower.follow(startToScore());
    }

    @Override
    public void loop() {
        follower.update();

        if (!follower.following() && step == 0) {
            step = 1;
            follower.follow(park());
        }

        telemetry.addData("X", follower.pose().x());
        telemetry.addData("Y", follower.pose().y());
        telemetry.addData(
                "Heading (degrees)",
                Math.toDegrees(follower.pose().heading())
        );
        telemetry.addData("Step", step);
        telemetry.update();
    }
}
