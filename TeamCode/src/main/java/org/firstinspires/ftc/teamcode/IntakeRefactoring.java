package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Gamepad;
public class IntakeRefactoring {
    private DcMotor intakeMotor;
    private Servo flowerServo;
    private DcMotor elevatorMotor;
    private double currentServoPosition = RobotConfig.FLOWER_SERVO_UP_POSITION;
    private boolean lastTriggerState = false;
    private boolean motorIsSpinning = false;
    private boolean lastButtonState = false;

    public IntakeRefactoring(HardwareMap hardwareMap) {
        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        flowerServo = hardwareMap.get(Servo.class, "flowerServo");
        elevatorMotor = hardwareMap.get(DcMotor.class, "elevatorMotor");
        flowerServo.setDirection(Servo.Direction.REVERSE);
        flowerServo.setPosition(RobotConfig.FLOWER_SERVO_UP_POSITION);
    }
public void update(Gamepad gamepad){

    boolean currentButtonState = gamepad.left_bumper;
    if (currentButtonState && !lastButtonState) {
        motorIsSpinning = !motorIsSpinning;
    }
    lastButtonState = currentButtonState;

    if (gamepad.right_bumper) {
        intakeMotor.setPower(-RobotConfig.INTAKE_POWER);
        elevatorMotor.setPower(-RobotConfig.ELEVATOR_POWER);
    } else if (motorIsSpinning) {
        intakeMotor.setPower(RobotConfig.INTAKE_POWER);
        elevatorMotor.setPower(RobotConfig.ELEVATOR_POWER);
    } else {
        intakeMotor.setPower(0.0);
        elevatorMotor.setPower(0.0);
    }

    boolean currentTriggerState = gamepad.left_trigger > 0.5;
    if (currentTriggerState && !lastTriggerState) {
        if (currentServoPosition == RobotConfig.FLOWER_SERVO_DOWN_POSITION) {
            currentServoPosition = RobotConfig.FLOWER_SERVO_UP_POSITION;
        } else {
            currentServoPosition = RobotConfig.FLOWER_SERVO_DOWN_POSITION;
        }
    }
    lastTriggerState = currentTriggerState;
    flowerServo.setPosition(currentServoPosition);
}
}
