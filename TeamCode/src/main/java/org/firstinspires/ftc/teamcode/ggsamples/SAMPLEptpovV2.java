//import
package org.firstinspires.ftc.teamcode.ggsamples;

import android.content.Context;
import android.graphics.Color;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.HardWareGrade;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.List;
import java.util.Locale;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@TeleOp(name = "SAMPLEptpovV2", group = "Pushbot")
@Disabled
public class SAMPLEptpovV2 extends LinearOpMode {
    HardWareGrade robot = new HardWareGrade();
    //channels
    public final int motorFrontLeftChannel = 0;
    public final int motorFrontRightChannel = 1;
    public final int motorBackLeftChannel = 2;
    public final int motorBackRightChannel = 3;
    public final String sparkShortChannel = "e0";
    public final String sparkLongChannel = "e1";
    public final int spearChannel = 0;
    public final String imuChannel = "e i2c 0";
    public final String digitalTouchChannel = "i2c 1";
    public final String sensorColorChannel = "i2c 2";
    public final String sensorDistanceChannel = "i2c 3";
    //names
    public final String motorBackLeftChannelName = "motorBackLeft    ";
    public final String motorBackRightChannelName = " motorBackRight ";
    public final String motorFrontLeftChannelName = "motorFrontLeft ";
    public final String motorFrontRightChannelName = "motorFrontRight";
    public final String digitalTouchChannelName = "digital_touch";
    public final String sensorColorChannelName = "sensor_color";
    public final String sensorDistanceChannelName = "distance1";
    public final String sparkShortChannelName = "sparkShort";
    public final String sparkLongChannelName = "sparkLong";
    public final String spearChannelName = "spear";
    //adaptable arm
    //public double rotationalXMultiplier=0;//used for adaptable arm
    //public double rotationalYMultiplier=0;//used for adaptable arm
//initiations
    //devices
    //camera
    public double spot = 0;
    public String recog = null;
    private static final String TFOD_MODEL_ASSET = "PowerPlay.tflite";
    private static final String[] LABELS = {
            "1 Bolt",
            "2 Bulb",
            "3 Panel"
    };
    private static final String VUFORIA_KEY =
            "AXmzBcj/////AAABme5HSJ/H3Ucup73WSIaV87tx/sFHYaWfor9OZVg6afr2Bw7kNolHd+mF5Ps91SlQpgBHulieI0jcd86kqJSwx46BZ8v8DS5S5x//eQWMEGjMDnvco4/oTcDwuSOLIVZG2UtLmJXPS1L3CipjabePFlqAL2JtBlN78p6ZZbRFSHW680hWEMSimZuQy/cMudD7J/MjMjMs7b925b8BkijlnTQYr7CbSlXrpDh5K+9fLlk2OyEZ4w7tm7e4UJDInJ/T3oi8PqqKCqkUaTkJWlQsvoELbDu5L2FgzsuDhBLe2rHtJRqfORd7n+6M30UdFSsxqq5TaZztkWgzRUr1GC3yBSTS6iFqEuL3g06GrfwOJF0F";
    public VuforiaLocalizer vuforia;
    public TFObjectDetector tfod;
    //push sensor
    public DigitalChannel digitalTouch;
    // color sensor
    public NormalizedColorSensor sensor_color;
    //distance
    public DistanceSensor distance1;   //distance sensor
    public Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) distance1;//helps init distance sensors
    //imu ( inside expansion hub )
    public BNO055IMU imu;    //imu module inside expansion hub
    public Orientation angles;     //imu uses these to find angles and classify them
    public Acceleration gravity;    //Imu uses to get acceleration
    // led
    RevBlinkinLedDriver blinkinLedDriver;//init led driver
    RevBlinkinLedDriver.BlinkinPattern pattern;//led driver pattern
    Telemetry.Item patternName;//shows pattern
    Telemetry.Item display;
    // org.firstinspires.ftc.teamcode.SampleRevBlinkinLedDriver.DisplayKind displayKind;//gives display kind between manual and auto
    Deadline ledCycleDeadline;
    //vars
    public double position = 0;//sets servo position to 0-1 multiplier
    public final double degree_mult = 0.00555555554;//100/180
    //Multiplies this by degrees to see exact position in degrees
    //distance
    public double MM_distance1 = 0;//mm distance for distance sensor 1
    public double CM_distance1 = 0;//cm distance for distance sensor 1
    public double M_distance1 = 0;//m distance for distance sensor 1
    public double IN_distance1 = 0;//in distance for distance sensor 1
    //servo
    public double sparkLongHelp = 0;//will increase to set position higher
    public double sparkShortHelp = 0;
    public final double spearHelper = 180;//full value to go out and in
    //color
    final float[] hsvValues = new float[3];//gets values for color sensor
    public int redVal = 0;//the red value in rgb
    public int greenVal = 0;//the green value in rgb
    public int blueVal = 0;//the blue value in rgb
    public String colorName = "N/A";//gets color name
    //debug
    public final boolean debug_mode = false;//debug mode
    public final boolean testing_mode = true;//test mode
    public String lastButtonPressed = "N/A";//last button pressed//debug will help with this
    double numero = 0;
    //slowmode
    double slowMode = 0; //0 is off
    final double regular_divider = 1;  //tells slowmode how fast to go when not on
    final double slowMode_divider = 2; //half speed when slowmode
    //range
    boolean inRange = false;//tested to see if distance sensor is in range
    boolean updated_inRange = false;//tests again to a boolean for if in range
    boolean updatedHeadingInRange = false;//heading check for low to high
    //endgame
    boolean endgame = false;                 // Use to prevent multiple half-time warning rumbles.
    final double End_Game = 118.0;              // Wait this many seconds before rumble-alert for half-time.
    //sounds
    String[] sounds = {"ss_alarm", "ss_bb8_down", "ss_bb8_up", "ss_darth_vader", "ss_fly_by",
            "ss_mf_fail", "ss_laser", "ss_laser_burst", "ss_light_saber", "ss_light_saber_long", "ss_light_saber_short",
            "ss_light_speed", "ss_mine", "ss_power_up", "ss_r2d2_up", "ss_roger_roger", "ss_siren", "ss_wookie"};
    boolean soundPlaying = false; //finds if the sound is actually playing
    //encoders
    static final double COUNTS_PER_MOTOR_REV = 1200;    // eg: TETRIX Motor Encoder//counts per rotation
    static final double DRIVE_GEAR_REDUCTION = .05;     // This is < 1.0 if geared UP//how much gears
    static final double WHEEL_DIAMETER_INCHES = 4.6950;     // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);//to get the counts per inch
    //telemetry
    public String direction_FW;//string of direction
    public String direction_LR;//string of direction
    public String direction_TLR;//string of direction
    public String slowModeON;//slowmode string ex(on or off)
    public String direction_ANGLE;//string of angle
    public String pushSensorCheck;//shows value from push sensor
    public double headingVal = 0;//heading in degrees
    public double alteredHeading = 0;//in case expansion hub is mounted in
    // different direction than facing forward
// init vars (used in initiation process)
    public double counter = 0;//counts initiations//prevents telemetry clogging
    public double extra_counter = 0;//counts initiations
    public boolean colors = false;//tells to init
    public boolean setupGuide = true;
    public boolean distance = false;//tells to init
    public boolean sound = false;//tells to init
    public boolean imuInit = false;//tells to init
    public boolean LED = false;//tells to init
    public boolean camera = false;
    public boolean push = false;//tells to init
    public boolean picture = true;
    public String statusVal = "OFFLINE";
    double frontLeftPower = 0;
    double backLeftPower = 0;
    double frontRightPower = 0;
    double backRightPower = 0;
    //public double deg=0;
    //gear ratio
    final int gear_ratio = 15;
    public int reduction = 0;

    //run opmode
    @Override
    public void runOpMode() {
        init_controls(true, false, true, false);//initiates everything
        robot.init(hardwareMap);
        if (imuInit) {
            BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
            parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
            parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
            parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
            parameters.loggingEnabled = true;
            parameters.loggingTag = "IMU";
            parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
            imu = hardwareMap.get(BNO055IMU.class, "imu1");
            imu.initialize(parameters);
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            gravity = imu.getGravity();
        }
        updateStatus("INIT");//sets status
        //init all motors
        if (push) {//init push sensor
            digitalTouch = hardwareMap.get(DigitalChannel.class, (digitalTouchChannelName));
        }
        if (colors) {//init all color things
            sensor_color = hardwareMap.get(NormalizedColorSensor.class, (sensorColorChannelName));
        }
        if (distance) {//initiates all distance data
            distance1 = hardwareMap.get(DistanceSensor.class, (sensorDistanceChannelName));
        }
        if (camera) {
            initVuforia();
            initTfod();
            if (tfod != null) {
                tfod.activate();
                tfod.setZoom(1.0, 16.0 / 9.0);
            }
        }
        //other initiates
        ElapsedTime runtime = new ElapsedTime();//runtime helps with endgame initiation and cues
        if (imuInit) {//will set up everything for imu
            imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
            composeTelemetry();
        }
        waitForStart();
        if (isStopRequested()) return;
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                runtime.reset();
                getRuntime();//gets runtime
                init_controls(false, true, true, false);//only imu if first init//initiates everything
                double y = gamepad1.left_stick_y; // Remember, this is reversed!//forward backward
                double x = -gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing//left right
                double rx = -gamepad1.right_stick_x;//turning
                double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);//gets max value
                frontLeftPower = (y + x + rx) / denominator;
                backLeftPower = (y - x + rx) / denominator;
                frontRightPower = (y - x - rx) / denominator;
                backRightPower = (y + x - rx) / denominator;
                //slowmode
                if (gamepad1.b && slowMode == 0) {//checks for button press and slowmode off
                    slowMode = 1;//sets slowmode to on
                    if (debug_mode) {
                        getLastButtonPress("b");
                    }
                } else if (gamepad1.b && slowMode == 1) {//checks for button press and slowmode on
                    slowMode = 0;//sets slowmode to off
                    if (debug_mode) {
                        getLastButtonPress("b");
                    }
                }
                if (slowMode == 1) {
                    backRightPower /= slowMode_divider;//divides power by the divider
                    backLeftPower /= slowMode_divider;//divides power by the divider
                    frontRightPower /= slowMode_divider;//divides power by the divider
                    frontLeftPower /= slowMode_divider;//divides power by the divider
                } else {
                    backRightPower /= regular_divider;
                    backLeftPower /= regular_divider;
                    frontRightPower /= regular_divider;
                    frontLeftPower /= regular_divider;
                }
                //
            }
            //endgame init
            if ((runtime.seconds() > End_Game) && !endgame) {//sets endgame
                endgame = true;
            }
            if (!endgame) {
                telemetry.addData(">", "Almost ENDGAME: %3.0f Sec \n", (End_Game - runtime.seconds()));//shows time til endgame
            }
            //
            //sets power to respective motors
            setAllDrivePower(frontRightPower, frontLeftPower, backRightPower, backLeftPower);
            robot.sparkLong.setPower(sparkLongHelp);
            robot.sparkShort.setPower(sparkShortHelp);
            //motorFrontLeft.setPower(frontLeftPower);
            teleSpace();//puts a space in telemetry
            if (camera) {
                runVu();
            }
            //fun stuff/ useful but not useful
            if (distance) {
                telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));//distance sensor
                telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));//distance sensor
            }
            if (picture) {
                picInTele(0);//might slow down considerably
            }
            telemetry.update();
            sleep(50);
        }
    }

    //not even close to finished
    //sense sleeve
    public void sleeveSense() {
        showFeedback();
        //robot.spear.setPosition(setServo((int) spearHelper));
        while (CM_distance1 >= 5) {
            //move closer
        }
        while (CM_distance1 <= 5) {
            //move back
        }
        if (CM_distance1 == 1) {//prime distance
            if (colorName.equals("red")) {
                //do this
            }
            if (colorName.equals("blue")) {
                //do this
            }
            if (colorName.equals("black")) {
                //do this
            }
        }
        //robot.spear.setPosition(setServo((int) -spearHelper));
    }

    //experimental
    public void gearRatio(int ratio) {
        reduction = 20 / ratio;
    }

    public void setAllDrivePower(double fr, double fl, double br, double bl) {
        fr /= reduction;
        fl /= reduction;
        br /= reduction;
        bl /= reduction;
        robot.motorFrontRight.setPower(fr);
        robot.motorFrontLeft.setPower(fl);
        robot.motorBackRight.setPower(br);
        robot.motorBackLeft.setPower(bl);
    }

    public void setupGuide() {
        //motors
        telemetry.addData("motorFrontLeft", String.valueOf((motorFrontLeftChannel)), motorFrontLeftChannelName);
        telemetry.addData("motorBackLeft", String.valueOf(motorBackLeftChannel), (motorBackLeftChannelName));
        telemetry.addData("motorFrontRight", String.valueOf(motorFrontRightChannel), motorFrontRightChannelName);
        telemetry.addData("motorBackRight", String.valueOf(motorBackRightChannel), motorBackRightChannelName);
        //servos
        telemetry.addData("spear", String.valueOf(spearChannel), spearChannelName);
        telemetry.addData("sparkLong", String.valueOf(sparkLongChannel), sparkLongChannelName);
        telemetry.addData("sparkShort", String.valueOf(sparkShortChannel), sparkShortChannelName);
        //sensors
        telemetry.addData("digitalTouch", String.valueOf(digitalTouchChannel), digitalTouchChannelName);
        telemetry.addData("sensorColor", String.valueOf(sensorColorChannel), sensorColorChannelName);
        telemetry.addData("sensorDistance", String.valueOf(sensorDistanceChannel), sensorDistanceChannelName);
        telemetry.addData("imu", String.valueOf(imuChannel));
    }

    public void updateStatus(String status) {
        statusVal = status;
    }//set a new controller/game status

    //setServo//this sets the servo to a position based off a given degree
    //ex: servo.setPosition(setServo(90))
    public double setServo(int degrees) {
        position = degree_mult * degrees;
        return position;
    }

    public double milli_seconds(int seconds) {
        numero = 1000 * seconds;
        return numero;
    }

    public void getLastButtonPress(String button) {
        lastButtonPressed = button;
    }//gets last button press

    public void dance(String direction_1) {//-1=back//1=forward//fun little thing we learned from others
        updateStatus("Dancing");
        if (direction_1.equals("backwards")) {
            robot.motorFrontLeft.setPower(1);
            robot.motorBackLeft.setPower(-1);
            robot.motorFrontRight.setPower(1);
            robot.motorBackRight.setPower(-1);
        }
        if (direction_1.equals("forwards")) {
            robot.motorFrontLeft.setPower(-1);
            robot.motorBackLeft.setPower(1);
            robot.motorFrontRight.setPower(-1);
            robot.motorBackRight.setPower(1);
        }
    }

    //non-expiremental
//init
    //will initiate based on variables and assign variables
    public void init_controls(boolean first, boolean encoder, boolean controls, boolean reduction) {
        if (reduction) {
            gearRatio(gear_ratio);
        }
        if (first) {
            updateStatus("INIT");
        } else {
            updateStatus("RUNNING");
        }
        showFeedback();//gives feedback on telemetry
        counter += 1;

        if (sound) {//sounds
            extra_counter += 1;
        }
        if (distance) {
            counter += 1;
        }
        //only on first initiation
        if (first) {
            if (encoder) {//only on first init and if the encoder variable is true
                resetEncoder();
                counter += 1;
            }
            if (imuInit) {
                counter += 1;
            }
            if (camera) {
                counter += 1;
            }
        }
        if (colors) {//will init the color sensor values
            counter += 1;
        }
        if (LED) {
            init_LED();
            extra_counter += 1;
            //if (displayKind == org.firstinspires.ftc.teamcode.SampleRevBlinkinLedDriver.DisplayKind.AUTO) {
            doAutoDisplay();
            //}
        }
        if (controls) {
            showControls();
        }
    }

    //controls to be shown on telemetry
    public void showControls() {
        telemetry.addData("Control 1", "Driver");
        telemetry.addData("Control 2", "Other controls");
        telemetry.addData("Control 1", "b = slowmode");
        if (sound) {
            telemetry.addData("Control 2", "dpad up/down = cycle songs");
            telemetry.addData("Control 2", "A = play song");
        }
    }

    //telemetry
    //make space in telemetry read-out
    public void teleSpace() {
        telemetry.addLine();
    }

    //telemetry additions
    public void showFeedback() {
        //get variables for telemetry
        if (debug_mode) {
            telemetry.addData(" |__|                  ", "");
            telemetry.addData(" (o-)                   ", "");
            telemetry.addData("//||\\                  ", "");
        }
        if (testing_mode) {
            telemetry.addData(" Testing Mode 1...2...3...                  ", "");
        }
        telemetry.addData("Status", statusVal);//shows current status
        if (setupGuide) {
            setupGuide();
        }
        //gets direction vertical
        if (testing_mode) {
            if (gamepad1.left_stick_y < 0) {
                direction_FW = "forward";
            }
            if (gamepad1.left_stick_y > 0) {
                direction_FW = "backward";
            }
            if (gamepad1.left_stick_y == 0) {
                direction_FW = "idle";
            }

            //gets direction horizontal
            if (gamepad1.left_stick_x > 0) {
                direction_LR = "right";
            }
            if (gamepad1.left_stick_x < 0) {
                direction_LR = "left";
            }
            if (gamepad1.left_stick_x == 0) {
                direction_LR = "idle";
            }
            //gets turn angle
            if (gamepad1.right_stick_x > 0) {
                direction_TLR = "right";
            }
            if (gamepad1.right_stick_x < 0) {
                direction_TLR = "left";
            }
            if (gamepad1.right_stick_x == 0) {
                direction_TLR = "idle";
            }
            //shows slowmode status
            if (slowMode == 1) {
                slowModeON = "True";
            } else {
                slowModeON = "False";
            }
        }
        //direction heading
        if (imuInit) {
            getHeading();
            telemetry.addData("Heading", "%.1f", headingVal);
            telemetry.addData("Heading Direction", direction_ANGLE);
            if (testing_mode) {
                if (headingVal > 45 && headingVal < 135) {
                    direction_ANGLE = "right";
                }
                if (headingVal > -45 && headingVal < 45) {
                    direction_ANGLE = "forward";
                }
                if (headingVal < 135 && headingVal > -135) {
                    direction_ANGLE = "backwards";
                }
                if (headingVal < -45 && headingVal > -135) {
                    direction_ANGLE = "left";
                }
            }
        }
        //checks push sensor
        if (testing_mode) {
            if (push) {
                digitalTouch.getState();
                if (digitalTouch.getState()) {
                    pushSensorCheck = "Not Pressed";
                } else {
                    pushSensorCheck = "Pressed";
                }
            }
        }
        //shows all previously defined values
        if (testing_mode) {
            telemetry.addLine()
                    .addData("direction", direction_FW)
                    .addData("strafe", direction_LR)
                    .addData("turn", direction_TLR)
                    .addData("r trigger", "%.2f", gamepad1.right_trigger)
                    .addData("l trigger", "%.2f", gamepad1.left_trigger);
            teleSpace();
            telemetry.addData("slowMode", slowModeON);
        }
        teleSpace();
        //gives color values
        if (colors) {
            NormalizedRGBA colors = sensor_color.getNormalizedColors();
            Color.colorToHSV(colors.toColor(), hsvValues);
            telemetry.addLine()
                    .addData("Red", "%.3f", colors.red)
                    .addData("Green", "%.3f", colors.green)
                    .addData("Blue", "%.3f", colors.blue)
                    .addData("Hue", "%.3f", hsvValues[0])
                    .addData("Saturation", "%.3f", hsvValues[1])
                    .addData("Value", "%.3f", hsvValues[2])
                    .addData("Alpha", "%.3f", colors.alpha);
            get_color_name(colors.red, colors.green, colors.blue);
            telemetry.addLine()
                    .addData("Color", colorName)
                    .addData("RGB", "(" + redVal + "," + greenVal + "," + blueVal + ")");//shows rgb value
        }
        teleSpace();
        if (testing_mode) {
            if (push) {
                telemetry.addData("Digital Touch", pushSensorCheck);
            }
        }
        if (distance) {
            getDistance1(true);//gets and shows distances
            telemetry.addData("Distance Result", verifyDistance(1));
        }
        teleSpace();
        telemetry.addData("Init:", (counter) + "/6");
        telemetry.addData("Extra Init:", (extra_counter) + "/2");
    }

    //imu telemetry
    void composeTelemetry() {
        getHeading();
        // At the beginning of each telemetry update, grab a bunch of data
        // from the IMU that we will then display in separate lines.
        telemetry.addAction(new Runnable() {
            @Override
            public void run() {
                // Acquiring the angles is relatively expensive; we don't want
                // to do that in each of the three items that need that info, as that's
                // three times the necessary expense.
                angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
                gravity = imu.getGravity();
            }
        });

        telemetry.addLine()
                .addData("status", new Func<String>() {
                    @Override
                    public String value() {
                        return imu.getSystemStatus().toShortString();
                    }
                })
                .addData("calib", new Func<String>() {
                    @Override
                    public String value() {
                        return imu.getCalibrationStatus().toString();
                    }
                });
        telemetry.addLine()
                .addData("heading", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.firstAngle);
                    }
                })
                .addData("roll", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.secondAngle);
                    }
                })
                .addData("pitch", new Func<String>() {
                    @Override
                    public String value() {
                        return formatAngle(angles.angleUnit, angles.thirdAngle);
                    }
                });
    }

    //distance
    public void getDistance1(boolean give) {
        MM_distance1 = distance1.getDistance(DistanceUnit.MM);
        CM_distance1 = distance1.getDistance(DistanceUnit.CM);
        M_distance1 = distance1.getDistance(DistanceUnit.METER);
        IN_distance1 = distance1.getDistance(DistanceUnit.INCH);
        if (give) {
            giveDistances();
        }
    }

    public void giveDistances() {
        telemetry.addLine()
                .addData("distance", String.format("%.0001f mm", MM_distance1))
                .addData("distance", String.format("%.0001f cm", CM_distance1))
                .addData("distance", String.format("%.0001f m", M_distance1))
                .addData("distance", String.format("%.0001f in", IN_distance1));
    }

    public String verifyDistance(int sensor_number) {
        if (sensor_number == 1) {
            getDistance1(false);
            if ((CM_distance1 * 10 != MM_distance1) || (M_distance1 * 10 != CM_distance1)) {
                return "Distance isn't perfect";
            } else {
                return "Distance is perfect";
            }
        }
        return null;
    }

    // helper functions
    //colors
    public void get_color_name(float red, float green, float blue) {
        if ((red <= 1) && (red >= 0.9375) && (green <= 1) && (green >= 0.8671875) && (blue <= 1) && (blue >= 0.67578125)) {
            colorName = "white";
        }
        if ((red <= 0.5) && (red >= 0) && (green <= 1) && (green >= 0.59765625) && (blue <= 1) && (blue >= 0.44921875)) {
            colorName = "blue";
        }
        if ((red <= 0.5) && (red >= 0) && (green <= 0.5) && (green >= 0) && (blue <= 0.5) && (blue >= 0)) {
            colorName = "black";
        }
        if ((red <= 1) && (red >= 0.3984375) && (green <= 0.234375) && (green >= 0) && (blue <= 0.5) && (blue >= 0)) {
            colorName = "red";
        }
        getColorRGB(red, green, blue);
    }

    public void getColorRGB(float red, float green, float blue) {
        redVal = (int) (red * 256);
        greenVal = (int) (green * 256);
        blueVal = (int) (blue * 256);
    }

    //imu
    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }

    public void getHeading() {
        float firstAngle = angles.firstAngle;
        headingVal = firstAngle + alteredHeading;
    }

    //range
    //gets the values and finds if it is in a range of max to min
    //if (checkDistance(1,10,1,"cm"))==True{}
    public boolean checkDistance(int sensor_number, int maxD, int minD, String unit) {
        resetRanges();
        if (sensor_number == 1) {
            getDistance1(false);
            if (unit.equals("cm")) {
                if (CM_distance1 >= minD && CM_distance1 <= maxD) {
                    inRange = true;
                }
            } else if (unit.equals("mm")) {
                if (MM_distance1 >= minD && MM_distance1 <= maxD) {
                    inRange = true;
                }
            } else if (unit.equals("in")) {
                if (IN_distance1 >= minD && IN_distance1 <= maxD) {
                    inRange = true;
                }
            } else if (unit.equals("m")) {
                if (M_distance1 >= minD && M_distance1 <= maxD) {
                    inRange = true;
                }
            } else {
                inRange = false;
            }
        }
        return inRange;
    }

    // if (checkHeading(90,0))==True{}
    public boolean checkHeading(int maxH, int minH) {
        getHeading();
        resetRanges();
        updatedHeadingInRange = headingVal >= minH && headingVal <= maxH;
        return updatedHeadingInRange;
    }

    //resets range
    public void resetRanges() {
        updated_inRange = false;
        inRange = false;
    }

    //Led
    public void init_LED() {
        blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;
        blinkinLedDriver.setPattern(pattern);
        //display = telemetry.addData("Display Kind: ", displayKind.toString());
        patternName = telemetry.addData("Pattern: ", pattern.toString());
        //setDisplayKind();
    }
    //protected void setDisplayKind()
    //{
    //    this.displayKind = SampleRevBlinkinLedDriver.DisplayKind.AUTO;
    //    display.setValue(SampleRevBlinkinLedDriver.DisplayKind.AUTO.toString());
    //}

    protected void doAutoDisplay() {
        if (ledCycleDeadline.hasExpired()) {
            pattern = pattern.next();
            displayPattern();
            ledCycleDeadline.reset();
        }
    }

    protected void displayPattern() {
        blinkinLedDriver.setPattern(pattern);
        patternName.setValue(pattern.toString());
    }

    //encoder
    public void encoderDrive(double speed,
                             double leftInches, double rightInches) {
        int newLeftTarget;
        int newRightTarget;
        if (opModeIsActive()) {
            newLeftTarget = robot.motorFrontLeft.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = robot.motorFrontRight.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            robot.motorFrontLeft.setTargetPosition(newLeftTarget);
            robot.motorFrontRight.setTargetPosition(newRightTarget);
            robot.motorBackLeft.setTargetPosition(newLeftTarget);
            robot.motorBackRight.setTargetPosition(newRightTarget);
            robot.motorFrontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorFrontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorBackLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorBackRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorFrontLeft.setPower(Math.abs(speed));
            robot.motorFrontRight.setPower(Math.abs(speed));
            robot.motorBackLeft.setPower(Math.abs(speed));
            robot.motorBackRight.setPower(Math.abs(speed));
            while (opModeIsActive() &&
                    (robot.motorFrontRight.isBusy() && robot.motorBackRight.isBusy()
                            && robot.motorBackLeft.isBusy() && robot.motorFrontLeft.isBusy())) {
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d",
                        robot.motorFrontLeft.getCurrentPosition(),
                        robot.motorFrontRight.getCurrentPosition(),
                        robot.motorBackLeft.getCurrentPosition(),
                        robot.motorBackRight.getCurrentPosition());
            }
            robot.motorFrontLeft.setPower(0);
            robot.motorFrontRight.setPower(0);
            robot.motorBackLeft.setPower(0);
            robot.motorBackRight.setPower(0);
            robot.motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void resetEncoder() {
        telemetry.addData("Status", "Resetting Encoders");    //
        robot.motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        telemetry.addData("Path0", "Starting at %7d :%7d",
                robot.motorFrontRight.getCurrentPosition(),
                robot.motorFrontLeft.getCurrentPosition(),
                robot.motorBackLeft.getCurrentPosition(),
                robot.motorBackRight.getCurrentPosition());
    }

    //camera
    public void runVu() {
        if (tfod != null) {
            if (spot == 0) {
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    for (Recognition recognition : updatedRecognitions) {
                        double centerX = (recognition.getLeft() + recognition.getRight()) / 2;
                        double centerY = (recognition.getTop() + recognition.getBottom()) / 2;
                        double width = Math.abs(recognition.getRight() - recognition.getLeft());
                        double height = Math.abs(recognition.getTop() - recognition.getBottom());
                        telemetry.addData("", " ");
                        telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
                        telemetry.addData("- Position (Row/Col)", "%.0f / %.0f", centerX, centerY);
                        telemetry.addData("- Size (Width/Height)", "%.0f / %.0f", width, height);
                        recog = (recognition.getLabel());
                        if (spot == 0) {
                            if (recog == "1 Bolt") {
                                spot += 1;
                                //red
                            }
                            if (recog == "2 Bulb") {
                                spot += 2;
                                //blue
                            }
                            if (recog == "3 Panel") {
                                spot += 3;
                                //black
                            }
                            telemetry.addData("Spot:", String.valueOf(spot), (recog));
                        }
                    }
                }
            }
        }
    }

    public void initVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

    public void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.75f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 300;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
    }

    //picInTele
    //put cool thing in telemetry
    public void picInTele(int choice) {
        if (choice == 0) {
            teleSpace();
        }
        if (choice == 1) {
            telemetry.addLine().addData("▐▓█▀▀▀▀▀▀▀▀▀█▓▌░▄▄▄▄▄░                      ", "");
            telemetry.addLine().addData("▐▓█░░▀░░▀▄░░█▓▌░█▄▄▄█░                      ", "");
            telemetry.addLine().addData("▐▓█░░▄░░▄▀░░█▓▌░█▄▄▄█░                      ", "");
            telemetry.addLine().addData("▐▓█▄▄▄▄▄▄▄▄▄█▓▌░█████░                      ", "");
            telemetry.addLine().addData("░░░░▄▄███▄▄░░░░░█████░                      ", "");
        }
        if (choice == 2) {
            telemetry.addLine().addData("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$$$$$$$$$$$$$$$$$$$$$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$$$$$$$$$$$$$$$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$$$$$$$$$$$$$$$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$$____$$$____$$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$______$______$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$______$______$_____$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$____$$$$$$$$$$$$$$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$___$$___________$$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____$__$$_______________$$$$                      ", "");
            telemetry.addLine().addData("$$$$__________$$_____________$$$$                      ", "");
            telemetry.addLine().addData("$$$$___________$$___________$$$$$                      ", "");
            telemetry.addLine().addData("$$$$_____________$_________$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$$_____________________$$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$$$___________________$$$$$$$$                      ", "");
            telemetry.addLine().addData("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$                      ", "");
        }
        if (choice == 3) {
            telemetry.addLine().addData("─▄▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▄                       ", "");
            telemetry.addLine().addData("█░░░█░░░░░░░░░░▄▄░██░█                      ", "");
            telemetry.addLine().addData("█░▀▀█▀▀░▄▀░▄▀░░▀▀░▄▄░█                      ", "");
            telemetry.addLine().addData("█░░░▀░░░▄▄▄▄▄░░██░▀▀░█                      ", "");
            telemetry.addLine().addData("─▀▄▄▄▄▄▀─────▀▄▄▄▄▄▄▀                       ", "");
        }
        if (choice == 4) {
            telemetry.addLine().addData("──▄────▄▄▄▄▄▄▄────▄───                      ", "");
            telemetry.addLine().addData("─▀▀▄─▄█████████▄─▄▀▀──                      ", "");
            telemetry.addLine().addData("─────██─▀███▀─██──────                      ", "");
            telemetry.addLine().addData("───▄─▀████▀████▀─▄────                      ", "");
            telemetry.addLine().addData("─▀█────██▀█▀██────█▀──                      ", "");
        }
        if (choice == 5) {
            telemetry.addLine().addData("───▄█▌─▄─▄─▐█▄───                         ", "");
            telemetry.addLine().addData("───██▌▀▀▄▀▀▐██───                         ", "");
            telemetry.addLine().addData("───██▌─▄▄▄─▐██───                         ", "");
            telemetry.addLine().addData("───▀██▌▐█▌▐██▀───                         ", "");
            telemetry.addLine().addData("▄██████─▀─██████▄                         ", "");
        }
        if (choice == 6) {
            telemetry.addLine().addData("░░░░░░░▄█▄▄▄█▄░░░░░░░                      ", "");
            telemetry.addLine().addData("▄▀░░░░▄▌─▄─▄─▐▄░░░░▀▄                      ", "");
            telemetry.addLine().addData("█▄▄█░░▀▌─▀─▀─▐▀░░█▄▄█                      ", "");
            telemetry.addLine().addData("░▐▌░░░░▀▀███▀▀░░░░▐▌░                      ", "");
            telemetry.addLine().addData("████░▄█████████▄░████                      ", "");
        }
        if (choice == 7) {
            telemetry.addLine().addData("╭━┳━╭━╭━╮╮                             ", "");
            telemetry.addLine().addData("┃┈┈┈┣▅╋▅┫┃                            ", "");
            telemetry.addLine().addData("┃┈┃┈╰━╰━━━━━━╮                        ", "");
            telemetry.addLine().addData("╰┳╯┈┈┈┈┈┈┈┈┈◢▉◣                      ", "");
            telemetry.addLine().addData("╲┃┈┈┈┈┈┈┈┈┈┈▉▉▉                       ", "");
            telemetry.addLine().addData("╲┃┈┈┈┈┈┈┈┈┈┈◥▉◤                      ", "");
            telemetry.addLine().addData("╲┃┈┈┈┈╭━┳━━━━╯                        ", "");
            telemetry.addLine().addData("╲┣━━━━━━┫                             ", "");
        }
        if (choice == 8) {
            telemetry.addLine().addData("______________$$$$$$$                                           ", "");
            telemetry.addLine().addData("_____________$$$$$$$$$                                          ", "");
            telemetry.addLine().addData("____________$$$$$$$$$$$                                         ", "");
            telemetry.addLine().addData("____________$$$$$$$$$$$                                         ", "");
            telemetry.addLine().addData("____________$$$$$$$$$$$                                         ", "");
            telemetry.addLine().addData("_____________$$$$$$$$$                                          ", "");
            telemetry.addLine().addData("_____$$$$$$_____$$$$$$$$$$                                      ", "");
            telemetry.addLine().addData("____$$$$$$$$__$$$$$$_____$$$                                    ", "");
            telemetry.addLine().addData("___$$$$$$$$$$$$$$$$_________$                                   ", "");
            telemetry.addLine().addData("___$$$$$$$$$$$$$$$$______$__$                                   ", "");
            telemetry.addLine().addData("___$$$$$$$$$$$$$$$$_____$$$_$                                   ", "");
            telemetry.addLine().addData("___$$$$$$$$$$$__________$$$_$_____$$                            ", "");
            telemetry.addLine().addData("____$$$$$$$$$____________$$_$$$$_$$$$                           ", "");
            telemetry.addLine().addData("______$$$__$$__$$$______________$$$$                            ", "");
            telemetry.addLine().addData("___________$$____$_______________$                              ", "");
            telemetry.addLine().addData("____________$$____$______________$                              ", "");
            telemetry.addLine().addData("_____________$$___$$$__________$$                               ", "");
            telemetry.addLine().addData("_______________$$$_$$$$$$_$$$$$                                 ", "");
            telemetry.addLine().addData("________________$$____$$_$$$$$                                  ", "");
            telemetry.addLine().addData("_______________$$$$$___$$$$$$$$$$                               ", "");
            telemetry.addLine().addData("_______________$$$$$$$$$$$$$$$$$$$$                             ", "");
            telemetry.addLine().addData("_______________$$_$$$$$$$$$$$$$$__$$                            ", "");
            telemetry.addLine().addData("_______________$$__$$$$$$$$$$$___$_$                            ", "");
            telemetry.addLine().addData("______________$$$__$___$$$______$$$$                            ", "");
            telemetry.addLine().addData("______________$$$_$__________$$_$$$$                            ", "");
            telemetry.addLine().addData("______________$$$$$_________$$$$_$_$                            ", "");
            telemetry.addLine().addData("_______________$$$$__________$$$__$$                            ", "");
            telemetry.addLine().addData("_____$$$$_________$________________$                            ", "");
            telemetry.addLine().addData("___$$$___$$______$$$_____________$$                             ", "");
            telemetry.addLine().addData("__$___$$__$$_____$__$$$_____$$__$$                              ", "");
            telemetry.addLine().addData("_$$____$___$_______$$$$$$$$$$$$$                                ", "");
            telemetry.addLine().addData("_$$_____$___$_____$$$$$_$$___$$$                                ", "");
            telemetry.addLine().addData("_$$_____$___$___$$$$____$____$$                                 ", "");
            telemetry.addLine().addData("__$_____$$__$$$$$$$____$$_$$$$$                                 ", "");
            telemetry.addLine().addData("__$$_____$___$_$$_____$__$__$$$$$$$$$$$$                        ", "");
            telemetry.addLine().addData("___$_____$$__$_$_____$_$$$__$$__$______$$$                      ", "");
            telemetry.addLine().addData("____$$_________$___$$_$___$$__$$_________$                      ", "");
            telemetry.addLine().addData("_____$$_$$$$___$__$$__$__________________$                      ", "");
            telemetry.addLine().addData("______$$____$__$$$____$__________________$                      ", "");
            telemetry.addLine().addData("_______$____$__$_______$$______________$$                       ", "");
            telemetry.addLine().addData("_______$$$$_$$$_________$$$$$$$__$$$$$$                         ", "");
            telemetry.addLine().addData("__________$$$_________________$$$$$                             ", "");
        }
        if (choice == 9) {
            telemetry.addLine().addData("╔═╦═╗    ╔╗ ╔═══╗                      ", "");
            telemetry.addLine().addData("║║║║╠═╗╔═╣╚╗║╔══╝                      ", "");
            telemetry.addLine().addData("║║║║║╬╚╣═╣║║║╚══╗                      ", "");
            telemetry.addLine().addData("╚╩═╩╩══╩═╩╩╝╚══╗║                      ", "");
            telemetry.addLine().addData("            ╔══╝║                      ", "");
        }
    }
}
//5lines
//  ▐▓█▀▀▀▀▀▀▀▀▀█▓▌░▄▄▄▄▄░
//  ▐▓█░░▀░░▀▄░░█▓▌░█▄▄▄█░
//  ▐▓█░░▄░░▄▀░░█▓▌░█▄▄▄█░
//  ▐▓█▄▄▄▄▄▄▄▄▄█▓▌░█████░
//  ░░░░▄▄███▄▄░░░░░█████░
//16lines
//  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//  $$$$$$$$$$$$$$$$$$$$$$$$_____$$$$
//  $$$$_____$$$$$$$$$$$$$$$_____$$$$
//  $$$$_____$$$$$$$$$$$$$$$_____$$$$
//  $$$$_____$$____$$$____$$_____$$$$
//  $$$$_____$______$______$_____$$$$
//  $$$$_____$______$______$_____$$$$
//  $$$$_____$____$$$$$$$$$$$$$$$$$$$
//  $$$$_____$___$$___________$$$$$$$
//  $$$$_____$__$$_______________$$$$
//  $$$$__________$$_____________$$$$
//  $$$$___________$$___________$$$$$
//  $$$$_____________$_________$$$$$$
//  $$$$$_____________________$$$$$$$
//  $$$$$$___________________$$$$$$$$
//  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//5lines
//  ─▄▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▄
//  █░░░█░░░░░░░░░░▄▄░██░█
//  █░▀▀█▀▀░▄▀░▄▀░░▀▀░▄▄░█
//  █░░░▀░░░▄▄▄▄▄░░██░▀▀░█
//  ─▀▄▄▄▄▄▀─────▀▄▄▄▄▄▄▀
//5lines
//  ──▄────▄▄▄▄▄▄▄────▄───
//  ─▀▀▄─▄█████████▄─▄▀▀──
//  ─────██─▀███▀─██──────
//  ───▄─▀████▀████▀─▄────
//  ─▀█────██▀█▀██────█▀──
//5lines
//  ───▄█▌─▄─▄─▐█▄
//  ───██▌▀▀▄▀▀▐██
//  ───██▌─▄▄▄─▐██
//  ───▀██▌▐█▌▐██▀
//  ▄██████─▀─██████▄
//5lines
//  ░░░░░░░▄█▄▄▄█▄░░░░░░░
//  ▄▀░░░░▄▌─▄─▄─▐▄░░░░▀▄
//  █▄▄█░░▀▌─▀─▀─▐▀░░█▄▄█
//  ░▐▌░░░░▀▀███▀▀░░░░▐▌░
//  ████░▄█████████▄░████
//8lines
//  ╭━┳━╭━╭━╮╮
//  ┃┈┈┈┣▅╋▅┫┃
//  ┃┈┃┈╰━╰━━━━━━╮
//  ╰┳╯┈┈┈┈┈┈┈┈┈◢▉◣
//  ╲┃┈┈┈┈┈┈┈┈┈▉▉▉
//  ╲┃┈┈┈┈┈┈┈┈┈◥▉◤
//  ╲┃┈┈┈┈╭━┳━━━━╯
//  ╲┣━━━━━━┫
//42lines
//  ______________$$$$$$$
//  _____________$$$$$$$$$
//  ____________$$$$$$$$$$$
//  ____________$$$$$$$$$$$
//  ____________$$$$$$$$$$$
//  _____________$$$$$$$$$
//  _____$$$$$$_____$$$$$$$$$$
//  ____$$$$$$$$__$$$$$$_____$$$
//  ___$$$$$$$$$$$$$$$$_________$
//  ___$$$$$$$$$$$$$$$$______$__$
//  ___$$$$$$$$$$$$$$$$_____$$$_$
//  ___$$$$$$$$$$$__________$$$_$_____$$
//  ____$$$$$$$$$____________$$_$$$$_$$$$
//  ______$$$__$$__$$$______________$$$$
//  ___________$$____$_______________$
//  ____________$$____$______________$
//  _____________$$___$$$__________$$
//  _______________$$$_$$$$$$_$$$$$
//  ________________$$____$$_$$$$$
//  _______________$$$$$___$$$$$$$$$$
//  _______________$$$$$$$$$$$$$$$$$$$$
//  _______________$$_$$$$$$$$$$$$$$__$$
//  _______________$$__$$$$$$$$$$$___$_$
//  ______________$$$__$___$$$______$$$$
//  ______________$$$_$__________$$_$$$$
//  ______________$$$$$_________$$$$_$_$
//  _______________$$$$__________$$$__$$
//  _____$$$$_________$________________$
//  ___$$$___$$______$$$_____________$$
//  __$___$$__$$_____$__$$$_____$$__$$
//  _$$____$___$_______$$$$$$$$$$$$$
//  _$$_____$___$_____$$$$$_$$___$$$
//  _$$_____$___$___$$$$____$____$$
//  __$_____$$__$$$$$$$____$$_$$$$$
//  __$$_____$___$_$$_____$__$__$$$$$$$$$$$$
//  ___$_____$$__$_$_____$_$$$__$$__$______$$$
//  ____$$_________$___$$_$___$$__$$_________$
//  _____$$_$$$$___$__$$__$__________________$
//  ______$$____$__$$$____$__________________$
//  _______$____$__$_______$$______________$$
//  _______$$$$_$$$_________$$$$$$$__$$$$$$
//  __________$$$_________________$$$$$
//5lines
//  ╔═╦═╗████╔╗█╔═══╗
//  ║║║║╠═╗╔═╣╚╗║╔══╝
//  ║║║║║╬╚╣═╣║║║╚══╗
//  ╚╩═╩╩══╩═╩╩╝╚══╗║
//  ████████████╔══╝║
     