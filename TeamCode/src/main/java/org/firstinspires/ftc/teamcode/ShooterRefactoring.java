package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterRefactoring {
    private DcMotorEx pollenFlywheelMotor;
    private DcMotorEx nectarFlywheelMotor;
    private Servo pollenGateServo;
    private Servo nectarGateServo;
    private double pollenManualVelocity = RobotConfig.POLLEN_FLYWHEEL_VELOCITY;
    private double nectarManualVelocity = RobotConfig.NECTAR_FLYWHEEL_VELOCITY;
    private boolean lastGp2LeftTrigger = false;
    private boolean lastGp2LeftBumper = false;
    private boolean lastGp2RightTrigger = false;
    private boolean lastGp2RightBumper = false;
    public ShooterRefactoring(HardwareMap hardwareMap) {
        nectarFlywheelMotor = hardwareMap.get(DcMotorEx.class, "nectarFlywheelMotor");
        pollenFlywheelMotor = hardwareMap.get(DcMotorEx.class, "pollenFlywheelMotor");
        pollenGateServo = hardwareMap.get(Servo.class, "pollenGateServo");
        nectarGateServo = hardwareMap.get(Servo.class, "nectarGateServo");
        nectarFlywheelMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        nectarFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        pollenFlywheelMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        pollenFlywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        pollenManualVelocity = RobotConfig.POLLEN_FLYWHEEL_VELOCITY;
        nectarManualVelocity = RobotConfig.NECTAR_FLYWHEEL_VELOCITY;
    }
    public void update(Gamepad gp1, Gamepad gp2){
        boolean currentGp2LeftTrigger = gp2.left_trigger > 0.5;
        boolean currentGp2LeftBumper = gp2.left_bumper;
        boolean currentGp2RightTrigger = gp2.right_trigger > 0.5;
        boolean currentGp2RightBumper = gp2.right_bumper;

        // Pollen (Positive velocity: +1930)
        if (currentGp2LeftTrigger && !lastGp2LeftTrigger) {
            if (Math.abs(pollenManualVelocity) > 1500) {
                pollenManualVelocity -= 10.0;
            }
        }
        if (currentGp2LeftBumper && !lastGp2LeftBumper) {
            if (Math.abs(pollenManualVelocity) < 2500) {
                pollenManualVelocity += 10.0;
            }
        }

        // Nectar (Negative velocity: -2100)
        if (currentGp2RightTrigger && !lastGp2RightTrigger) {
            if (Math.abs(nectarManualVelocity) > 1500) {
                nectarManualVelocity += 10.0; // Moves closer to 0 (slower)
            }
        }
        if (currentGp2RightBumper && !lastGp2RightBumper) {
            if (Math.abs(nectarManualVelocity) < 2500) {
                nectarManualVelocity -= 10.0; // Moves further negative (faster)
            }
        }

        lastGp2LeftTrigger = currentGp2LeftTrigger;
        lastGp2LeftBumper = currentGp2LeftBumper;
        lastGp2RightTrigger = currentGp2RightTrigger;
        lastGp2RightBumper = currentGp2RightBumper;

        // 7. SHOOTER VELOCITY CHECK
        boolean driverWantsToScore = gp1.x || gp1.b;
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

        // 8. POLLEN GATE CONTROL
        if (gp1.x && pollenReady) {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_OPEN_POSITION);
        } else {
            pollenGateServo.setPosition(RobotConfig.POLLEN_GATE_SERVO_CLOSED_POSITION);
        }

        // 9. NECTAR GATE CONTROL
        if (gp1.b && nectarReady) {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_OPEN_POSITION);
        } else {
            nectarGateServo.setPosition(RobotConfig.NECTAR_GATE_SERVO_CLOSED_POSITION);
        }
    }
    public void sendTelemetry(Telemetry telemetry){
        telemetry.addData("Pollen Target", Math.abs(pollenManualVelocity));
        telemetry.addData("Nectar Target", Math.abs(nectarManualVelocity));
        telemetry.addData("Pollen Vel", Math.abs(pollenFlywheelMotor.getVelocity()));
        telemetry.addData("Nectar Vel", Math.abs(nectarFlywheelMotor.getVelocity()));
    }
}

