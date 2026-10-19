package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="Test Shooter Velocity", group="Linear OpMode")
public class ShooterTest extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();
    // 2. Change the type to DcMotorEx
    private DcMotorEx shooterMotor = null;

    private boolean motorIsSpinning = false;
    private boolean lastButtonState = false;

    // 3. Define your target velocity (Ticks per Second)
    // Adjust this number based on your motor's max RPM (e.g., 2000-2500 for a shooter)
    private final double TARGET_VELOCITY = 2000.0;

    @Override
    public void runOpMode() {
        // 4. Cast the hardware map retrieval to DcMotorEx
        shooterMotor = (DcMotorEx) hardwareMap.get(DcMotor.class, "ShooterMotor");

        // 5. CRITICAL: Reset encoders and set to RUN_USING_ENCODER
        // Without this, velocity commands will not work properly!
        shooterMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {

            boolean currentButtonState = gamepad1.right_trigger > 0.5;

            if (currentButtonState && !lastButtonState) {
                motorIsSpinning = !motorIsSpinning;
            }
            lastButtonState = currentButtonState;

            // 6. Use setVelocity() instead of setPower()
            if (motorIsSpinning) {
                shooterMotor.setVelocity(TARGET_VELOCITY);
            } else {
                shooterMotor.setVelocity(0.0);
            }

            // 7. Monitor performance via Telemetry
            telemetry.addData("Target Velocity", motorIsSpinning ? TARGET_VELOCITY : 0);
            telemetry.addData("Actual Velocity", shooterMotor.getVelocity()); // Reads current ticks/sec
            telemetry.update();
        }
    }
}
