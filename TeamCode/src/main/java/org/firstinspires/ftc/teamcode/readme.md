## TeamCode Module

Welcome!

This module, TeamCode, is the place where you will write/paste the code for your team's
robot controller App. This module is currently empty (a clean slate) but the
process for adding OpModes is straightforward.

## Dashboard testing and shared configuration

This project uses [Slothboard](https://github.com/Dairy-Foundation/Sloth#ftc-dashboard),
the Sloth-compatible FTC Dashboard replacement. Pedro's tuning dependency brings
in Sloth, so use Slothboard rather than installing standard FTC Dashboard alongside
it. The dependency is declared in `build.dependencies.gradle` and uses the existing
Dairy Maven repository. Java imports remain under `com.acmerobotics.dashboard`.

To try live configuration without moving any hardware, temporarily remove
`@Disabled` from `DashboardTest` before deploying:

1. Fully deploy TeamCode from Android Studio after changing dependencies.
2. Connect your computer to the Control Hub's Wi-Fi and open
   <http://192.168.43.1:8080/dash>.
3. Select **Dashboard Test** on the Driver Station and press **INIT** or start it.
4. In the dashboard's Configuration panel, edit **RobotConfig > TEST_VALUE** or
   **DashboardTest > OP_MODE_TEST_VALUE**, then use that panel's **Save Changes**
   button.
5. Verify that the corresponding telemetry value changes on both the dashboard
   and Driver Station. The loop count and elapsed seconds indicate live telemetry.

Put shared tunable fields in `RobotConfig` so any OpMode can read them directly,
for example `RobotConfig.TEST_VALUE`. Dashboard fields must be `public static`
and not `final`, and the enclosing class must have `@Config`. Read the field
where it is used instead of copying it once during initialization. Browser edits
do not modify Java source defaults or persist across app restarts.

Build the app locally with `./gradlew :TeamCode:assembleDebug` from the repository
root.

## Gaming TeleOp

**Gaming TeleOp** is the only enabled team OpMode. Other team OpModes have
`@Disabled`; remove that annotation to re-enable one. Pedro tuning registration
is also disabled; restore `@Tuner` on the factory methods in `pedro/Tuning.java`
and its import to re-enable the tuning procedures.

Select **Gaming TeleOp** on the Driver Station for robot-centric driving through
Pedro Pathing. Gamepad 1's left stick moves the robot forward/backward and
strafes left/right relative to the robot; the right stick's horizontal axis turns
it left/right. Stopping the OpMode zeros drivetrain power.

This OpMode uses the drivetrain and Pinpoint hardware configured in
`pedro/Constants.java`.
It also requires servos named `pollenGateServo`, `nectarGateServo`, and
`flowerServo`, and reports their last commanded positions (0-1) in telemetry
and commands both gates closed and the flower servo up during initialization.
These values are not measured physical positions.
Each press of gamepad 1's **B** button toggles `flowerServo` between
`RobotConfig.FLOWER_SERVO_DOWN_POSITION` (default 0.53) and
`RobotConfig.FLOWER_SERVO_UP_POSITION` (default 0.0), starting with down on
the first press. Edit these fields under **RobotConfig** in the dashboard and
save changes; the next B press uses the latest value for its selected endpoint.
Keep positions in the 0-1 range. The former minimum is up; the former maximum
is down.
Holding B does not repeatedly toggle it.

Each press of gamepad 1's **A** button toggles both gate servos between open and
closed, starting with open on the first press. Each gate has independent
dashboard endpoints: `POLLEN_GATE_SERVO_CLOSED_POSITION`,
`POLLEN_GATE_SERVO_OPEN_POSITION`, `NECTAR_GATE_SERVO_CLOSED_POSITION`, and
`NECTAR_GATE_SERVO_OPEN_POSITION` under **RobotConfig**. These replace the old
min/max fields, preserving min as closed and max as open. Pollen currently has
both endpoints at 0.0; nectar is closed at 0.37 and open at 0.52.
Saved dashboard edits apply on the next A press; holding A does not repeatedly
toggle the gates. Telemetry reports the commanded open/closed state.

Gamepad 1's **RT** toggles the motor named `intakeMotor` on/off when the trigger
crosses above 0.5. Release it to 0.5 or below before pressing again; holding RT
does not repeatedly toggle. Intake power comes from **RobotConfig > INTAKE_POWER**
(default 0.5, valid range -1 to 1). Saved dashboard edits apply while the intake
is running. The intake starts off and is powered off when the OpMode stops.
Telemetry displays its enabled state and commanded power.

Gamepad 1's **RB** toggles the motor named `elevatorMotor` on/off once per press.
Power comes from **RobotConfig > ELEVATOR_POWER** (default 0.5, valid range -1 to
1), with saved dashboard edits applied while running. The elevator starts off,
is powered off when the OpMode stops, and reports its enabled state and commanded
power in telemetry. Holding RB does not repeatedly toggle it.

Gamepad 1's **Y** toggles `pollenFlywheelMotor` and `nectarFlywheelMotor` on/off
together once per press. Each has independent power under **RobotConfig**:
`POLLEN_FLYWHEEL_POWER` and `NECTAR_FLYWHEEL_POWER` (both default 0.0, valid range
-1 to 1). With the defaults, enabling the flywheels does not spin them. Saved
dashboard edits apply while enabled. Both start off and are powered off when
the OpMode stops. Telemetry displays the shared enabled state and each motor's
commanded power. Holding Y does not repeatedly toggle them.

## Creating your own OpModes

The easiest way to create your own OpMode is to copy a Sample OpMode and make it your own.

Sample opmodes exist in the FtcRobotController module.
To locate these samples, find the FtcRobotController module in the "Project/Android" tab.

Expand the following tree elements:
 FtcRobotController/java/org.firstinspires.ftc.robotcontroller/external/samples

### Naming of Samples

To gain a better understanding of how the samples are organized, and how to interpret the
naming system, it will help to understand the conventions that were used during their creation.

These conventions are described (in detail) in the sample_conventions.md file in this folder.

To summarize: A range of different samples classes will reside in the java/external/samples.
The class names will follow a naming convention which indicates the purpose of each class.
The prefix of the name will be one of the following:

Basic:  	This is a minimally functional OpMode used to illustrate the skeleton/structure
            of a particular style of OpMode.  These are bare bones examples.

Sensor:    	This is a Sample OpMode that shows how to use a specific sensor.
            It is not intended to drive a functioning robot, it is simply showing the minimal code
            required to read and display the sensor values.

Robot:	    This is a Sample OpMode that assumes a simple two-motor (differential) drive base.
            It may be used to provide a common baseline driving OpMode, or
            to demonstrate how a particular sensor or concept can be used to navigate.

Concept:	This is a sample OpMode that illustrates performing a specific function or concept.
            These may be complex, but their operation should be explained clearly in the comments,
            or the comments should reference an external doc, guide or tutorial.
            Each OpMode should try to only demonstrate a single concept so they are easy to
            locate based on their name.  These OpModes may not produce a drivable robot.

After the prefix, other conventions will apply:

* Sensor class names are constructed as:    Sensor - Company - Type
* Robot class names are constructed as:     Robot - Mode - Action - OpModetype
* Concept class names are constructed as:   Concept - Topic - OpModetype

Once you are familiar with the range of samples available, you can choose one to be the
basis for your own robot.  In all cases, the desired sample(s) needs to be copied into
your TeamCode module to be used.

This is done inside Android Studio directly, using the following steps:

 1) Locate the desired sample class in the Project/Android tree.

 2) Right click on the sample class and select "Copy"

 3) Expand the  TeamCode/java folder

 4) Right click on the org.firstinspires.ftc.teamcode folder and select "Paste"

 5) You will be prompted for a class name for the copy.
    Choose something meaningful based on the purpose of this class.
    Start with a capital letter, and remember that there may be more similar classes later.

Once your copy has been created, you should prepare it for use on your robot.
This is done by adjusting the OpMode's name, and enabling it to be displayed on the
Driver Station's OpMode list.

Each OpMode sample class begins with several lines of code like the ones shown below:

```
 @TeleOp(name="Template: Linear OpMode", group="Linear Opmode")
 @Disabled
```

The name that will appear on the driver station's "opmode list" is defined by the code:
 ``name="Template: Linear OpMode"``
You can change what appears between the quotes to better describe your opmode.
The "group=" portion of the code can be used to help organize your list of OpModes.

As shown, the current OpMode will NOT appear on the driver station's OpMode list because of the
  ``@Disabled`` annotation which has been included.
This line can simply be deleted , or commented out, to make the OpMode visible.



## ADVANCED Multi-Team App management:  Cloning the TeamCode Module

In some situations, you have multiple teams in your club and you want them to all share
a common code organization, with each being able to *see* the others code but each having
their own team module with their own code that they maintain themselves.

In this situation, you might wish to clone the TeamCode module, once for each of these teams.
Each of the clones would then appear along side each other in the Android Studio module list,
together with the FtcRobotController module (and the original TeamCode module).

Selective Team phones can then be programmed by selecting the desired Module from the pulldown list
prior to clicking to the green Run arrow.

Warning:  This is not for the inexperienced Software developer.
You will need to be comfortable with File manipulations and managing Android Studio Modules.
These changes are performed OUTSIDE of Android Studios, so close Android Studios before you do this.
 
Also.. Make a full project backup before you start this :)

To clone TeamCode, do the following:

Note: Some names start with "Team" and others start with "team".  This is intentional.

1)  Using your operating system file management tools, copy the whole "TeamCode"
    folder to a sibling folder with a corresponding new name, eg: "Team0417".

2)  In the new Team0417 folder, delete the TeamCode.iml file.

3)  the new Team0417 folder, rename the "src/main/java/org/firstinspires/ftc/teamcode" folder
    to a matching name with a lowercase 'team' eg:  "team0417".

4)  In the new Team0417/src/main folder, edit the "AndroidManifest.xml" file, change the line that contains
         package="org.firstinspires.ftc.teamcode"
    to be
         package="org.firstinspires.ftc.team0417"

5)  Add:    include ':Team0417' to the "/settings.gradle" file.
    
6)  Open up Android Studios and clean out any old files by using the menu to "Build/Clean Project""