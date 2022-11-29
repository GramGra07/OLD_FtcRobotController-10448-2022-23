package org.firstinspires.ftc.teamcode.teleOp;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;
import java.util.Objects;


@TeleOp(name = "teleop", group = "Robot")//declaring the name and group of the opmode
//@Disabled//disabling the opmode
public class scrap extends LinearOpMode {//declaring the class
    private ElapsedTime runtime = new ElapsedTime();
    //encoder var
    public int turn = 77;
    public double yMult = 24;
    public double xMult = 10;
    public double ovrCurrX = 0;
    public double ovrCurrY = 0;
    public double ovrTurn = 0;
    public static final double COUNTS_PER_INCH_Side_dead = -665.08;
    public static final double COUNTS_PER_INCH_Side = -100;
    //offset
    public double rOffset = distanceSensorCalibrator.rOffset;
    public double lOffset = distanceSensorCalibrator.lOffset;//-5.1;
    public double fOffset = distanceSensorCalibrator.fOffset;
    public double bOffset = distanceSensorCalibrator.bOffset;
    //arm
    public static final double COUNTS_PER_MOTOR_REV_arm = 28;
    public static final double DRIVE_GEAR_REDUCTION_arm = 40;
    public static final double WHEEL_DIAMETER_INCHES_arm = 1.102;     // For figuring circumference
    public static final double COUNTS_PER_INCH_arm = (COUNTS_PER_MOTOR_REV_arm * DRIVE_GEAR_REDUCTION_arm) /
            (WHEEL_DIAMETER_INCHES_arm * 3.1415);
    //wheels
    public static final double COUNTS_PER_MOTOR_REV = 28;
    public static final double WHEEL_DIAMETER_MM = 96;
    public static final double DRIVE_GEAR_REDUCTION = 15;
    public static final double WHEEL_DIAMETER_INCHES = WHEEL_DIAMETER_MM * 0.0393701;     // For figuring circumference
    public static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    public static final double COUNTS_PER_MOTOR_REV_dead = 8192;
    public static final double WHEEL_DIAMETER_MM_dead = 96;
    public static final double WHEEL_DIAMETER_INCHES_dead = WHEEL_DIAMETER_MM_dead * 0.0393701;     // For figuring circumference
    public static final double COUNTS_PER_INCH_dead = (COUNTS_PER_MOTOR_REV_dead) /
            (WHEEL_DIAMETER_INCHES_dead * Math.PI);
    //other variables
    public boolean slowModeIsOn = false;//declaring the slowModeIsOn variable
    public boolean reversed = false;//declaring the reversed variable

    //servo variables
    public double position = 0;//sets servo position to 0-1 multiplier
    public final double degree_mult = 0.00555555554;//100/180
    public final int baseClawVal = 30;//declaring the baseClawVal variable
    public final int magicNumOpen = 60;//declaring the magicNumOpen variable
    public boolean clawOpen = false;//declaring the clawOpen variable

    //arm labels
    public final int baseArmPosition = 0;
    public static final int armLimit = 4150;//declaring the armLimit variable
    public final int baseArm = 100;//declaring the baseArm variable
    public static final int lowPoleVal = 1740;//should be about 1/3 of arm limit
    public static final int midPoleVal = 3100;//should be about 2/3 of arm limit
    public static final int fiveTallConeVal = 928;
    public final int topPoleVal = armLimit;//should be close to armLimit
    public boolean limiter = true;//declaring the limiter variable, is on or off
    public boolean limiting = false;//declaring the limiting variable

    //rumble
    Gamepad.RumbleEffect customRumbleEffect;//declaring the customRumbleEffect variable
    final double endgame = 120;//declaring the endgame variable
    public boolean isEndgame = false;//declaring the isEndgame variable
    Gamepad.RumbleEffect customRumbleEffect1;// declaring the customRumbleEffect1 variable
    public boolean rumble = false;//declaring the rumble variable
    final double end = 150;//declaring the end variable
    public boolean isEnd = false;//declaring the isEnd variable

    //rake
    public final int baseFlip = -90;//declaring the baseFlip variable
    public final int magicFlip = baseFlip + 50;//declaring the magicFlip variable
    public final int baseUnCone = 0;
    public final int magicUnCone = baseUnCone + 90;
    public boolean unConed = false;

    //motors/servos
    public DcMotor deadWheel = null;//declaring the deadWheel motor
    public DcMotor deadWheelL = null;//declaring the deadWheelL motor
    public DcMotor deadWheelR = null;//declaring the deadWheelR motor
    public DistanceSensor rDistance;//declaring the rDistance sensor
    public DistanceSensor lDistance;//declaring the lDistance sensor
    public DistanceSensor fDistance;//declaring the fDistance sensor
    public DistanceSensor bDistance;//declaring the bDistance sensor
    public DcMotor motorFrontLeft = null;
    public DcMotor motorBackLeft = null;
    public DcMotor motorFrontRight = null;
    public DcMotor motorBackRight = null;
    public DcMotor sparkLong = null;
    public Servo clawServo = null;
    public Servo unConer = null;
    public DigitalChannel red2;
    public DigitalChannel green2;

    //vuforia

    private static final String TFOD_MODEL_ASSET = "custom.tflite";
    private static final String[] LABELS = {
            "capacitor",//3
            "led",//1
            "resistor"//2
    };
    private static final String VUFORIA_KEY =
            "AXmzBcj/////AAABme5HSJ/H3Ucup73WSIaV87tx/sFHYaWfor9OZVg6afr2Bw7kNolHd+mF5Ps91SlQpgBHulieI0jcd86kqJSwx46BZ8v8DS5S5x//eQWMEGjMDnvco4/oTcDwuSOLIVZG2UtLmJXPS1L3CipjabePFlqAL2JtBlN78p6ZZbRFSHW680hWEMSimZuQy/cMudD7J/MjMjMs7b925b8BkijlnTQYr7CbSlXrpDh5K+9fLlk2OyEZ4w7tm7e4UJDInJ/T3oi8PqqKCqkUaTkJWlQsvoELbDu5L2FgzsuDhBLe2rHtJRqfORd7n+6M30UdFSsxqq5TaZztkWgzRUr1GC3yBSTS6iFqEuL3g06GrfwOJF0F";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private int spot = 0;
    //color
    final float[] hsvValues = new float[3];//gets values for color sensor
    private int redVal = 0;//the red value in rgb
    private int greenVal = 0;//the green value in rgb
    private int blueVal = 0;//the blue value in rgb
    private String colorName = "N/A";//gets color name
    NormalizedColorSensor colorSensor;//declaring the colorSensor variable
    //
    public String statusVal = "OFFLINE";
    public double fDistanceVal = 0;
    public double bDistanceVal = 0;
    public double lDistanceVal = 0;
    public double rDistanceVal = 0;
    //isRight side
    public boolean right = true;//declaring the right variable

    @Override
    public void runOpMode() {//if opmode is started
        updateStatus("Initializing");
        customRumbleEffect = new Gamepad.RumbleEffect.Builder()//build effect
                .addStep(1.0, 1.0, 250)
                .addStep(0.0, 0.0, 300)
                .addStep(1.0, 1.0, 250)
                .build();
        customRumbleEffect1 = new Gamepad.RumbleEffect.Builder()//build effect
                .addStep(1.0, 1.0, 200)
                .addStep(0.0, 0.0, 200)
                .addStep(1.0, 1.0, 200)
                .addStep(0.0, 0.0, 200)
                .addStep(1.0, 1.0, 1000)
                .build();
        ElapsedTime runtime = new ElapsedTime();//declaring the runtime variable
        rDistance = hardwareMap.get(DistanceSensor.class, "rDistance");//getting the rDistance sensor
        lDistance = hardwareMap.get(DistanceSensor.class, "lDistance");//getting the lDistance sensor
        fDistance = hardwareMap.get(DistanceSensor.class, "fDistance");//getting the fDistance sensor
        bDistance = hardwareMap.get(DistanceSensor.class, "bDistance");//getting the bDistance sensor
        DigitalChannel red1 = hardwareMap.get(DigitalChannel.class, "red1");//getting the red1 light
        DigitalChannel green1 = hardwareMap.get(DigitalChannel.class, "green1");//getting the green1 light
        DigitalChannel red2 = hardwareMap.get(DigitalChannel.class, "red2");//getting the red2 light
        DigitalChannel green2 = hardwareMap.get(DigitalChannel.class, "green2");//getting the green2 light
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensor");
        // Declare our motors
        // Make sure your ID's match your configuration
        DcMotor motorFrontLeft = hardwareMap.get(DcMotor.class, "motorFrontLeft");//getting the motorFrontLeft motor
        DcMotor motorBackLeft = hardwareMap.get(DcMotor.class, "motorBackLeft");//getting the motorBackLeft motor
        DcMotor motorFrontRight = hardwareMap.get(DcMotor.class, "motorFrontRight");//getting the motorFrontRight motor
        DcMotor motorBackRight = hardwareMap.get(DcMotor.class, "motorBackRight");//getting the motorBackRight motor
        deadWheel = hardwareMap.get(DcMotor.class, "deadWheel");//getting the deadWheel motor
        deadWheelL = hardwareMap.get(DcMotor.class, "deadWheelL");//getting the deadWheelL motor
        deadWheelR = hardwareMap.get(DcMotor.class, "deadWheelR");//getting the deadWheelR motor
        Servo clawServo = hardwareMap.get(Servo.class, "clawServo");//getting the clawServo servo
        Servo flipper = hardwareMap.get(Servo.class, "flipper");//getting the flipper servo
        Servo unConer = hardwareMap.get(Servo.class, "unConer");
        sparkLong = hardwareMap.get(DcMotor.class, "sparkLong");//getting the sparkLong motor

        sparkLong.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the sparkLong encoder
        motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the motorFrontLeft encoder
        motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the motorBackRight encoder
        motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the motorBackLeft encoder
        motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the motorFrontRight encoder
        deadWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the deadWheel encoder
        deadWheelL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the deadWheelL encoder
        deadWheelR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);//resetting the deadWheelR encoder

        motorFrontRight.setDirection(DcMotor.Direction.REVERSE);//setting the motorFrontRight direction
        motorBackRight.setDirection(DcMotor.Direction.REVERSE);//setting the motorBackRight direction

        sparkLong.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the sparkLong encoder to run using encoder
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the motorFrontLeft encoder to run using encoder
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the motorBackLeft encoder to run using encoder
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the motorBackRight encoder to run using encoder
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the motorFrontRight encoder to run using encoder
        deadWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the deadWheel encoder to run using encoder
        deadWheelL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the deadWheelL encoder to run using encoder
        deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);//setting the deadWheelR encoder to run using encoder

        motorBackRight.setZeroPowerBehavior(BRAKE);
        motorBackLeft.setZeroPowerBehavior(BRAKE);
        motorFrontRight.setZeroPowerBehavior(BRAKE);
        motorFrontLeft.setZeroPowerBehavior(BRAKE);
        sparkLong.setZeroPowerBehavior(BRAKE);
        red1.setMode(DigitalChannel.Mode.OUTPUT);//setting the red1 light to output
        green1.setMode(DigitalChannel.Mode.OUTPUT);//setting the green1 light to output
        red2.setMode(DigitalChannel.Mode.OUTPUT);//setting the red2 light to output
        green2.setMode(DigitalChannel.Mode.OUTPUT);//setting the green2 light to output

        //flipper.setPosition(setServo(magicFlip));//setting the flipper servo to the magicFlip position
        runtime.reset();//resetting the runtime variable
        waitForStart();//waiting for the start button to be pressed

        if (isStopRequested()) return;//if the stop button is pressed, stop the program

        while (opModeIsActive()) {//while the op mode is active
            if (gamepad1.touchpad_finger_1) {       //if the touchpad is pressed
                telemetry.addData("Finger 1: ", "%2f : %2f", gamepad1.touchpad_finger_1_x, gamepad1.touchpad_finger_1_y);
                if (gamepad1.touchpad_finger_1_x > 0) {//right side
                    right = true;
                }
                if (gamepad1.touchpad_finger_1_x < 0) {//left side
                    right = false;
                }
            }
            if (rumble) {
                if ((runtime.seconds() > endgame) && !isEndgame) {
                    gamepad1.runRumbleEffect(customRumbleEffect);
                    gamepad2.runRumbleEffect(customRumbleEffect);
                    isEndgame = true;
                }
                if ((runtime.seconds() > end) && !isEnd) {
                    gamepad1.runRumbleEffect(customRumbleEffect1);
                    gamepad2.runRumbleEffect(customRumbleEffect1);
                    isEnd = true;
                }
            }
            limiter = true;//make sure limiter is on


            double y = gamepad1.left_stick_y; // Remember, this is reversed!
            double x = -gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = -gamepad1.right_stick_x;
            double armPower = -gamepad2.left_stick_y;
            if (gamepad1.back) {
                //reverse controls
                reversed = !reversed;
            }
            if (!right) {
                reversed = true;
            }
            if (right) {
                reversed = false;
            }
            if (reversed) {
                y = -y;
                x = -x;
            }

            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;
            //armPower/=denominator;
            //switches
            if (gamepad1.left_trigger > 0) {
                slowModeIsOn = false;//toggle
            }
            if (gamepad1.right_trigger > 0) {
                slowModeIsOn = true;//toggle
            }
            //
            //flipper
            if (gamepad1.dpad_up) {
                flipper.setPosition(setServo(magicFlip));
            } else if (gamepad1.dpad_down) {
                flipper.setPosition(setServo(baseFlip));
            }
            if (gamepad1.dpad_right) {
                unConer.setPosition(setServo(magicUnCone));
            } else if (!unConed) {
                unConer.setPosition(setServo(baseUnCone));
            }
            //
            //
            if (sparkLong.getCurrentPosition() >= armLimit - 200 || sparkLong.getCurrentPosition() <= baseArm + 500) {
                green2.setState(false);
                red2.setState(true);
                limiting = true;
            } else {
                green2.setState(true);
                red2.setState(false);
                limiting = false;
            }
            //arm extend controller 2
            if (sparkLong.getCurrentPosition() <= armLimit && limiter) {
                sparkLong.setPower(armPower);
            } else {
                sparkLong.setPower(0);
            }
            //
            //claw code
            if (gamepad2.left_bumper) {
                clawServo.setPosition(setServo(magicNumOpen));
                clawOpen = true;
                //open claw
            } else if (gamepad2.right_bumper) {
                clawServo.setPosition(setServo(baseClawVal));
                //close claw
                clawOpen = false;
            }//else{
            //    clawServo.setPosition(setServo(0));
            //}//auto close
            if (clawOpen) {
                green1.setState(false);
                red1.setState(true);
            } else {
                green1.setState(true);
                red1.setState(false);
            }
            //
            //set values
            if ((slowModeIsOn)) {//is false
                frontLeftPower /= 4;
                backLeftPower /= 4;
                frontRightPower /= 4;
                backRightPower /= 4;
            }

            //
            //presets
            if (gamepad2.y) {//top level
                if (sparkLong.getCurrentPosition() < topPoleVal) {//go up
                    sparkLong.setPower(-1);
                } else {
                    sparkLong.setPower(0);
                }
            }
            if (gamepad2.a) {//base
                if (sparkLong.getCurrentPosition() > baseArm) {//go down
                    sparkLong.setPower(1);
                } else {
                    sparkLong.setPower(0);
                }
            }
            if (gamepad2.b) {//middle
                if (sparkLong.getCurrentPosition() > midPoleVal + 50) {//go down
                    sparkLong.setPower(1);
                }
                if (sparkLong.getCurrentPosition() < midPoleVal - 50) {//go up
                    sparkLong.setPower(-1);
                } else {
                    sparkLong.setPower(0);
                }
            }
            if (gamepad2.x) {//low
                if (sparkLong.getCurrentPosition() > lowPoleVal + 50) {//go down
                    sparkLong.setPower(-1);
                }
                if (sparkLong.getCurrentPosition() < lowPoleVal - 50) {//go up
                    sparkLong.setPower(1);
                } else {
                    sparkLong.setPower(0);
                }
            }
            //
            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);
            telemetry.addData("Status", statusVal);//shows current status
            telemetry.addLine("Limiter")
                    .addData("Val", String.valueOf(sparkLong.getCurrentPosition()))
                    .addData("Max", armLimit)
                    .addData("Limiter", limiter)
                    .addData("Is broken", (sparkLong.getCurrentPosition() > armLimit));
            //.addData("Is Limiting",limiting);
            telemetry.addData("reversed", reversed);
            telemetry.addData("slowMode", slowModeIsOn);
            telemetry.addData("dead", deadWheel.getCurrentPosition());
            telemetry.addData("deadR", deadWheelR.getCurrentPosition());
            telemetry.addData("deadL", deadWheelL.getCurrentPosition());
            teleSpace();
            //getAllColor();
            //teleSpace();
            distanceTelemetry();
            updateStatus("Running");
            telemetry.update();
        }
    }

    public void getAllColor() {
        //gives color values
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        Color.colorToHSV(colors.toColor(), hsvValues);
        getColorRGB(colors.red, colors.green, colors.blue);
        get_color_name(colors.red, colors.green, colors.blue);
        telemetry.addLine()
                .addData("Red", "%.3f", colors.red)
                .addData("Green", "%.3f", colors.green)
                .addData("Blue", "%.3f", colors.blue)
                .addData("Hue", "%.3f", hsvValues[0])
                .addData("Saturation", "%.3f", hsvValues[1])
                .addData("Value", "%.3f", hsvValues[2])
                .addData("Alpha", "%.3f", colors.alpha);
        telemetry.addLine()
                .addData("Color", colorName)
                .addData("RGB", "(" + redVal + "," + greenVal + "," + blueVal + ")");//shows rgb value
    }

    public void setUniPower(double power) {
        motorFrontLeft.setPower(power);
        motorBackLeft.setPower(power);
        motorFrontRight.setPower(power);
        motorBackRight.setPower(power);
    }

    public void teleSpace() {
        telemetry.addLine();
    }

    public void updateStatus(String status) {
        statusVal = status;
    }//set a new controller/game status

    public int getAverage(double firstVal, double secondVal) {
        return (int) ((firstVal + secondVal) / 2);
    }

    public void openClaw() {
        clawServo.setPosition(setServo(magicNumOpen));
    }

    public void closeClaw() {
        clawServo.setPosition(setServo(baseClawVal));
    }

    public void dropArm(int prevHeight) {
        armEncoder(prevHeight - 50, 0.5, 2, true);
        openClaw();
    }


    public void distanceTelemetry() {
        updateDistance();
        telemetry.addLine("Distance")
                .addData("", "")
                .addData("f", String.valueOf(fDistanceVal))
                .addData("b", String.valueOf(bDistanceVal))
                .addData("l", String.valueOf(lDistanceVal))
                .addData("r", String.valueOf(rDistanceVal));
    }

    public void situate() {
        encoderDrive(1, 4, 4, 1);
    }


    public void encoderComboFwd(double speed, double lInches, double rInches,
                                double pose, int timeoutS, boolean isUp) {
        int newLeftTarget;
        int newRightTarget;
        int newDLeftTarget;
        int newDRightTarget;
        int target;
        target = (int) pose;
        sparkLong.setTargetPosition(target);
        sparkLong.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        newLeftTarget = motorBackLeft.getCurrentPosition() + (int) (lInches * COUNTS_PER_INCH);
        newRightTarget = motorBackRight.getCurrentPosition() + (int) (rInches * COUNTS_PER_INCH);
        newDLeftTarget = deadWheelL.getCurrentPosition() + (int) (lInches * COUNTS_PER_INCH_dead);
        newDRightTarget = deadWheelR.getCurrentPosition() + (int) (rInches * COUNTS_PER_INCH_dead);

        deadWheelL.setTargetPosition(-newDLeftTarget);
        deadWheelR.setTargetPosition(-newDRightTarget);
        motorFrontRight.setTargetPosition(-newRightTarget);
        motorBackRight.setTargetPosition(-newRightTarget);
        motorFrontLeft.setTargetPosition(-newLeftTarget);
        motorBackLeft.setTargetPosition(-newLeftTarget);

        motorBackRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorBackLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        deadWheelL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        deadWheelR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();
        if (isUp) {
            sparkLong.setPower(speed);//go down
        }
        if (!isUp) {
            sparkLong.setPower(-speed);
        }
        motorBackLeft.setPower((speed));
        motorFrontRight.setPower((speed));
        motorFrontLeft.setPower((speed));
        motorBackRight.setPower((speed));
        while (opModeIsActive() &&
                (runtime.seconds() < timeoutS) && sparkLong.isBusy()) {

            // Display it for the driver.
            telemetry.addData(" arm Running to", sparkLong.getCurrentPosition());
            telemetry.addData("arm Currently at",
                    sparkLong.getCurrentPosition());
            // Display it for the driver.
            telemetry.update();
        }

        // Stop all motion;
        motorBackLeft.setPower(0);
        motorFrontRight.setPower(0);
        motorBackRight.setPower(0);
        motorFrontLeft.setPower(0);

        sparkLong.setPower(0);
        sparkLong.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // Turn off RUN_TO_POSITION
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        resetEncoders();
        telemetry.update();
    }

    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newLeftTarget;
        int newRightTarget;
        int newDLeftTarget;
        int newDRightTarget;
        if (opModeIsActive()) {

            newLeftTarget = motorBackLeft.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = motorBackRight.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            newDLeftTarget = deadWheelL.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH_dead);
            newDRightTarget = deadWheelR.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH_dead);

            deadWheelL.setTargetPosition(-newDLeftTarget);
            deadWheelR.setTargetPosition(-newDRightTarget);
            motorFrontRight.setTargetPosition(-newRightTarget);
            motorBackRight.setTargetPosition(-newRightTarget);
            motorFrontLeft.setTargetPosition(-newLeftTarget);
            motorBackLeft.setTargetPosition(-newLeftTarget);

            motorBackRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorBackLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorFrontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            deadWheelL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            deadWheelR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();
            motorBackLeft.setPower((speed));
            motorFrontRight.setPower((speed));
            motorFrontLeft.setPower((speed));
            motorBackRight.setPower((speed));
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (motorBackLeft.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Running to", "%7d :%7d", -newDLeftTarget, -newDRightTarget);//"%7d :%7d"
                telemetry.addData("Currently at", "%7d :%7d",
                        deadWheelL.getCurrentPosition(), deadWheelR.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            motorBackLeft.setPower(0);
            motorFrontRight.setPower(0);
            motorBackRight.setPower(0);
            motorFrontLeft.setPower(0);

            // Turn off RUN_TO_POSITION
            motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            resetEncoders();
        }
    }

    public void sideWaysEncoderDrive(double speed,
                                     double inches,
                                     double timeoutS) {//+=right //-=left
        int newFRTarget;
        int newFLTarget;
        int newBRTarget;
        int newBLTarget;
        int newDeadTarget;
        inches *= -1;
        if (opModeIsActive()) {
            if (inches < 0) {
                newFLTarget = motorFrontLeft.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newBLTarget = motorBackLeft.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newFRTarget = motorFrontRight.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newBRTarget = motorBackRight.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newDeadTarget = deadWheel.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side_dead);
                motorFrontLeft.setTargetPosition(-newFLTarget);
                motorBackLeft.setTargetPosition(newBLTarget);
                motorBackRight.setTargetPosition(-newBRTarget - 10);
                motorFrontRight.setTargetPosition(newFRTarget);
                deadWheel.setTargetPosition(-newDeadTarget);
            }
            if (inches > 0) {
                newFLTarget = motorFrontLeft.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newBLTarget = motorBackLeft.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newFRTarget = motorFrontRight.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newBRTarget = motorBackRight.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side);
                newDeadTarget = deadWheel.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_Side_dead);
                motorFrontLeft.setTargetPosition(-newFLTarget);
                motorBackLeft.setTargetPosition(newBLTarget);
                motorBackRight.setTargetPosition(-newBRTarget);
                motorFrontRight.setTargetPosition(newFRTarget);
                deadWheel.setTargetPosition(newDeadTarget);
            }

            motorFrontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorBackLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorBackRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            deadWheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();
            motorBackLeft.setPower(Math.abs(speed));
            motorFrontRight.setPower(Math.abs(speed));
            motorFrontLeft.setPower(Math.abs(speed));
            motorBackRight.setPower(Math.abs(speed));
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) && deadWheel.isBusy()) {

                // Display it for the driver.
                telemetry.addData("Running to", "%7d:%7d", motorFrontLeft.getCurrentPosition()
                        , motorBackRight.getCurrentPosition());
                telemetry.addData("Running to", "%7d:%7d", motorBackLeft.getCurrentPosition()
                        , motorFrontRight.getCurrentPosition());
                telemetry.addData("Currently at", "%7d:%7d",
                        motorFrontLeft.getCurrentPosition()
                        , motorBackRight.getCurrentPosition());
                telemetry.addData("Currently at", "%7d:%7d",
                        motorFrontRight.getCurrentPosition()
                        , motorBackLeft.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            motorBackLeft.setPower(0);
            motorFrontRight.setPower(0);
            motorFrontLeft.setPower(0);
            motorBackRight.setPower(0);

            // Turn off RUN_TO_POSITION
            motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            deadWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            resetEncoders();
        }
    }

    public void armEncoder(double pose, double speed, int timeOut, boolean isUp) {
        int target;
        target = (int) pose;
        sparkLong.setTargetPosition(target);
        sparkLong.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        runtime.reset();
        if (isUp) {
            sparkLong.setPower(speed);//go down
        }
        if (!isUp) {
            sparkLong.setPower(-speed);
        }
        while (opModeIsActive() &&
                (runtime.seconds() < timeOut) && sparkLong.isBusy()) {

            // Display it for the driver.
            telemetry.addData("Running to", sparkLong.getCurrentPosition());
            telemetry.addData("Currently at",
                    sparkLong.getCurrentPosition());
            telemetry.update();
        }
        sparkLong.setPower(0);
        sparkLong.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        telemetry.update();
    }

    public void resetEncoders() {
        motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        deadWheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        deadWheelL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        deadWheelR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //sparkLong.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public double setServo(int degrees) {
        position = degree_mult * degrees;
        return position;
    }

    public void updateDistance() {
        fDistanceVal = (fDistance.getDistance(DistanceUnit.CM) + fOffset);
        bDistanceVal = (bDistance.getDistance(DistanceUnit.CM) + bOffset);
        lDistanceVal = (lDistance.getDistance(DistanceUnit.CM) + lOffset);
        rDistanceVal = (rDistance.getDistance(DistanceUnit.CM) + rOffset);
    }

    public void adjustWDist(String sensor, double dist, double power, boolean closerThanDist) {//less than goes opposite way
        updateDistance();
        if (Objects.equals(sensor, "f")) {
            if (!closerThanDist) {
                while (fDistanceVal > dist) {
                    updateDistance();
                    fDistanceVal = (fDistance.getDistance(DistanceUnit.CM) + fOffset);
                    telemetry.addData("Distance", fDistanceVal);
                    telemetry.update();
                    motorFrontLeft.setPower(-power);
                    motorFrontRight.setPower(-power);
                    motorBackLeft.setPower(-power);
                    motorBackRight.setPower(-power);
                    if (fDistanceVal <= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
            if (closerThanDist) {
                power = -power;
                while (fDistanceVal < dist) {
                    updateDistance();
                    fDistanceVal = (fDistance.getDistance(DistanceUnit.CM) + fOffset);
                    telemetry.addData("Distance", fDistanceVal);
                    telemetry.update();
                    motorFrontLeft.setPower(-power);
                    motorFrontRight.setPower(-power);
                    motorBackLeft.setPower(-power);
                    motorBackRight.setPower(-power);
                    if (fDistanceVal >= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
        }
        if (Objects.equals(sensor, "b")) {
            if (!closerThanDist) {
                while (bDistanceVal > dist) {
                    updateDistance();
                    telemetry.addData("Distance", bDistanceVal);
                    telemetry.update();
                    bDistanceVal = (bDistance.getDistance(DistanceUnit.CM) + bOffset);
                    motorFrontLeft.setPower(power);
                    motorFrontRight.setPower(power);
                    motorBackLeft.setPower(power);
                    motorBackRight.setPower(power);
                    if (bDistanceVal <= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
            if (closerThanDist) {
                power = -power;
                while (bDistanceVal < dist) {
                    updateDistance();
                    telemetry.addData("Distance", bDistanceVal);
                    telemetry.update();
                    bDistanceVal = (bDistance.getDistance(DistanceUnit.CM) + bOffset);
                    motorFrontLeft.setPower(power);
                    motorFrontRight.setPower(power);
                    motorBackLeft.setPower(power);
                    motorBackRight.setPower(power);
                    if (bDistanceVal >= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
        }
        if (Objects.equals(sensor, "r")) {//shift right
            if (!closerThanDist) {
                while (rDistanceVal > dist) {
                    updateDistance();
                    telemetry.addData("Distance", rDistanceVal);
                    telemetry.update();
                    rDistanceVal = (rDistance.getDistance(DistanceUnit.CM) + rOffset);
                    motorFrontLeft.setPower(-power);
                    motorFrontRight.setPower(power);
                    motorBackLeft.setPower(power);
                    motorBackRight.setPower(-power);
                    if (rDistanceVal <= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
            if (closerThanDist) {
                while (rDistanceVal < dist) {
                    updateDistance();
                    telemetry.addData("Distance", rDistanceVal);
                    telemetry.update();
                    rDistanceVal = (rDistance.getDistance(DistanceUnit.CM) + rOffset);
                    motorFrontLeft.setPower(power);
                    motorFrontRight.setPower(-power);
                    motorBackLeft.setPower(-power);
                    motorBackRight.setPower(power);
                    if (rDistanceVal >= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
        }
        if (Objects.equals(sensor, "l")) {//shift left
            if (!closerThanDist) {
                while (lDistanceVal > dist) {
                    updateDistance();
                    telemetry.addData("Distance", lDistanceVal);
                    telemetry.update();
                    lDistanceVal = (lDistance.getDistance(DistanceUnit.CM) + lOffset);
                    motorFrontLeft.setPower(power);
                    motorFrontRight.setPower(-power);
                    motorBackLeft.setPower(-power);
                    motorBackRight.setPower(power);
                    if (lDistanceVal <= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
            if (closerThanDist) {
                while (lDistanceVal < dist) {
                    updateDistance();
                    telemetry.addData("Distance", lDistanceVal);
                    telemetry.update();
                    lDistanceVal = (lDistance.getDistance(DistanceUnit.CM) + lOffset);
                    motorFrontLeft.setPower(-power);
                    motorFrontRight.setPower(power);
                    motorBackLeft.setPower(power);
                    motorBackRight.setPower(-power);
                    if (lDistanceVal >= dist) {
                        motorFrontLeft.setPower(0);
                        motorFrontRight.setPower(0);
                        motorBackLeft.setPower(0);
                        motorBackRight.setPower(0);
                    }
                }
            }
        }
    }

    public void setOvr(double x, double y) {
        ovrCurrX = x;
        ovrCurrY = y;
    }

    public void advGoSpot(double currX, double currY, double targetX, double targetY, double power, boolean combo, int pose
            , boolean isUp, String orientation, double orientationVal, boolean endTurn, int turn,
                          boolean checkDistanceX, boolean checkDistanceY, String xSensor, double xDist, boolean lessThanX,
                          String ySensor, double yDist, boolean lessThanY) {
        if (ovrTurn % 180 == 0) {
            double mult = ovrTurn / 180;
            orientationVal *= (Math.pow(-1, mult));
        }
        if (Objects.equals(orientation, "|")) {
            double sidewaysInches = (targetX - currX) * xMult;
            double fwdInches = (targetY - currY) * yMult;
            fwdInches *= orientationVal;
            sidewaysInches *= orientationVal;
            telemetry.addData("fwdInches", fwdInches);
            telemetry.addData("sidewaysInches", sidewaysInches);
            if (currX < targetX) {
                sideWaysEncoderDrive(power, sidewaysInches, 6);
            } else if (currX > targetX) {
                sideWaysEncoderDrive(power, -sidewaysInches, 6);
            }
            if (currY < targetY) {
                if (!combo) {
                    encoderDrive(power, fwdInches, fwdInches, 6);
                } else {
                    encoderComboFwd(power, fwdInches, fwdInches, pose, 6, isUp);
                }
            } else if (currY > targetY) {
                if (!combo) {
                    encoderDrive(power, -fwdInches, -fwdInches, 6);
                } else {
                    encoderComboFwd(power, -fwdInches, -fwdInches, pose, 6, isUp);
                }
            }
        }
        if (Objects.equals(orientation, "-")) {
            double sidewaysInches = (targetY - currY) * xMult;
            double fwdInches = (targetX - currX) * yMult;
            if (orientationVal == -90 || orientationVal == 90) {
                orientationVal /= 90;
            }
            fwdInches *= orientationVal;
            sidewaysInches *= orientationVal;
            telemetry.addData("fwdInches", fwdInches);
            telemetry.addData("sidewaysInches", sidewaysInches);
            if (currY < targetY) {
                sideWaysEncoderDrive(power, sidewaysInches, 6);
            } else if (currY > targetY) {
                sideWaysEncoderDrive(power, -sidewaysInches, 6);
            }
            if (currX < targetX) {
                if (!combo) {
                    encoderDrive(power, fwdInches, fwdInches, 6);
                } else {
                    encoderComboFwd(power, fwdInches, fwdInches, pose, 6, isUp);
                }
            } else if (currX > targetX) {
                if (!combo) {
                    encoderDrive(power, -fwdInches, -fwdInches, 6);
                } else {
                    encoderComboFwd(power, -fwdInches, -fwdInches, pose, 6, isUp);
                }
            }
        }
        if (checkDistanceX) {
            adjustWDist(xSensor, xDist, power, lessThanX);
        }
        if (checkDistanceY) {
            adjustWDist(ySensor, yDist, power, lessThanY);
        }
        if (endTurn) {
            turn(turn);
        }
        setOvr(targetX, targetY);
        ovrTurn += turn;
        //telemetry.addData("orientation", orientation);
        telemetry.update();
        //sleep(5000);
    }

    public void turn(int degrees) {
        if (degrees > 180) {
            degrees = (360 - degrees) * -1;
        }
        int mult = 360 / (degrees + 1);
        int inches = (turn / mult);
        encoderDrive(0.65, -inches, inches, 6);
        resetEncoders();
    }

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

    public void runVu(int timeoutS, boolean giveSpot) {
        runtime.reset();
        while (opModeIsActive() && (spot == 0)) {
            if (runtime.seconds() > timeoutS) {
                spot = 4;
            }
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Objects Detected", updatedRecognitions.size());

                    // step through the list of recognitions and display image position/size information for each one
                    // Note: "Image number" refers to the randomized image orientation/number
                    for (Recognition recognition : updatedRecognitions) {
                        double col = (recognition.getLeft() + recognition.getRight()) / 2;
                        double row = (recognition.getTop() + recognition.getBottom()) / 2;
                        double width = Math.abs(recognition.getRight() - recognition.getLeft());
                        double height = Math.abs(recognition.getTop() - recognition.getBottom());

                        telemetry.addData("", " ");
                        telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
                        telemetry.addData("- Position (Row/Col)", "%.0f / %.0f", row, col);
                        telemetry.addData("- Size (Width/Height)", "%.0f / %.0f", width, height);
                        if (giveSpot && spot == 0) {
                            if (Objects.equals(recognition.getLabel(), "led")) {
                                spot += 1;
                                break;
                            }
                            if (Objects.equals(recognition.getLabel(), "resistor")) {
                                spot += 2;
                                break;
                            }
                            if (Objects.equals(recognition.getLabel(), "capacitor")) {
                                spot += 3;
                                break;
                            }
                        }
                    }
                    telemetry.update();
                }
            }
        }
    }

    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.75f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 300;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);

        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
    }
}