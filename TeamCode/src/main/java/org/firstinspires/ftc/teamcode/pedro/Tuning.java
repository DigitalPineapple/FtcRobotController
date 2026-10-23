package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;

public class Tuning {
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }
    public static Procedure tests() {
        return new Tests(
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig),
                h -> new PinpointLocalizer(h, Constants.localizerConfig),
                () -> new Foresight(Constants.foresightConfig));
    }
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    public static Procedure foresightTuner() {
        return new ForesightTuner(
                (hardwareMap) -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                (hardwareMap) -> new Mecanum(hardwareMap, Constants.drivetrainConfig));
    }
}
