package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("leftFrontMotor");
        c.frontRightName.set("rightFrontMotor");
        c.backLeftName.set("leftBackMotor");
        c.backRightName.set("rightBackMotor");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });
    ;
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
            c.name.set("Pinpoint");
            c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            c.xPodOffset.set(5.514841755544106);
            c.yPodOffset.set(-2.4764780359944023);
            c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
            c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
            c.globalDistanceUnit.set(DistanceUnit.INCH);
            c.offsetUnits.set(DistanceUnit.INCH);
        });

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.24671201663881942);
                Controller secondaryTranslationalForward = Controller.proportional(0.09115349788154767);
                Controller primaryTranslationalLateral = Controller.proportional(0.4069344773731505);
                Controller secondaryTranslationalLateral = Controller.proportional(0.15035141589988382);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.016508103672949376));
                c.brake.set(Controller.proportionalFeedforward(0.01403188812200697));

                c.headingFeedback.set(Controller.proportional(4.455198197937236));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04767224323607276, 0.00472144587585353));

                c.linearBrakeCoefficients.set(Matrix.diag(0.10463627746380177, 0.06689054671485029));
                c.quadraticBrakeCoefficients.set(Matrix.diag(5.863840669243948E-4, 0.001266433405777084));

                c.maxAchievableForwardVelocity.set(62.15391638102305);
                c.maxAchievableStrafeVelocity.set(49.488240939973124);
                c.naturalForwardDeceleration.set(40.82513219706005);
                c.naturalStrafeDeceleration.set(73.91340766024257);
            }
    );}