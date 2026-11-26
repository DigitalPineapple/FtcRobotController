package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

public class DriveRefactoring {
    private Follower follower;
    private boolean lastOptionsButton = false;

    public DriveRefactoring(HardwareMap hardwareMap) {
        follower = Constants.create(hardwareMap);
    }

    public void setStartPose() {
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
    }

    public void update(Gamepad gamepad) {
        // 1. FIELD ORIENTATION RESET (Gamepad Options)
        boolean currentOptionsButton = gamepad.options;
        if (currentOptionsButton && !lastOptionsButton) {
            Pose resetPose = new Pose(follower.pose().x(), follower.pose().y(), 0.0);
            follower.setPose(resetPose);
        }
        lastOptionsButton = currentOptionsButton;

        // 2. FIELD-CENTRIC DRIVING
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad.left_stick_y,
                -gamepad.left_stick_x,
                -gamepad.right_stick_x,
                follower.pose().heading()
        );
        follower.manual(powers);
        follower.update();
    }

    public void sendTelemetry(Telemetry telemetry) {
        telemetry.addData("Heading", Math.toDegrees(follower.pose().heading()));
    }
}
