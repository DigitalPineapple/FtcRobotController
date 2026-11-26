package org.firstinspires.ftc.teamcode;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Field Qualifier TeleOp")
public class TeleOpRefactoring extends OpMode {

    private DriveRefactoring drive;
    private IntakeRefactoring intake;
    private ShooterRefactoring shooter;
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        drive = new DriveRefactoring(hardwareMap);
        intake = new IntakeRefactoring(hardwareMap);
        shooter = new ShooterRefactoring(hardwareMap);

        telemetry.addData("Status", "Initialized Cleanly!");
        telemetry.update();
    }

    @Override
    public void start() {
        drive.setStartPose();
        runtime.reset();
    }

    @Override
    public void loop() {
        drive.update(gamepad1);
        intake.update(gamepad1);
        shooter.update(gamepad1, gamepad2);

        drive.sendTelemetry(telemetry);
        shooter.sendTelemetry(telemetry);
        telemetry.addData("Runtime", runtime.toString());
        telemetry.update();
    }
}

