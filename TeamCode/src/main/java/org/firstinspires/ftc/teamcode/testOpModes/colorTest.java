package org.firstinspires.ftc.teamcode.testOpModes;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import android.graphics.Color;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.teleOp.robotCentric;

import java.util.List;
import java.util.Objects;

@TeleOp(name = "colorTest", group = "Robot")
@Disabled
public class colorTest extends robotCentric {
    public int turn = 77;


    private ElapsedTime runtime = new ElapsedTime();

    static final double COUNTS_PER_MOTOR_REV = 28;
    static final double WHEEL_DIAMETER_MM = 96;
    static final double WHEEL_DIAMETER_INCHES = WHEEL_DIAMETER_MM * 0.0393701;     // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * 15) /
            (WHEEL_DIAMETER_INCHES * Math.PI);

    static final double COUNTS_PER_MOTOR_REV_dead = 8192;
    static final double WHEEL_DIAMETER_MM_dead = 96;
    static final double WHEEL_DIAMETER_INCHES_dead = WHEEL_DIAMETER_MM_dead * 0.0393701;     // For figuring circumference
    static final double COUNTS_PER_INCH_dead = (COUNTS_PER_MOTOR_REV_dead) /
            (WHEEL_DIAMETER_INCHES_dead * Math.PI);

    static final double ROBOT_DIAMETER = 13.05;
    //arm
    final int baseArmPosition = 0;
    public final int armLimit = robotCentric.armLimit;
    public final int lowPoleVal = robotCentric.lowPoleVal;//should be about 1/3 of arm limit
    public final int midPoleVal = robotCentric.midPoleVal;//should be about 2/3 of arm limit
    public final int topPoleVal = armLimit;//should be close to armLimit
    static final double COUNTS_PER_MOTOR_REV_arm = 28;
    static final double DRIVE_GEAR_REDUCTION_arm = 40;
    static final double WHEEL_DIAMETER_INCHES_arm = 1.102;     // For figuring circumference
    static final double COUNTS_PER_INCH_arm = (COUNTS_PER_MOTOR_REV_arm * DRIVE_GEAR_REDUCTION_arm) /
            (WHEEL_DIAMETER_INCHES_arm * 3.1415);

    static final double COUNTS_PER_INCH_Side_dead = -665.08;
    static final double COUNTS_PER_INCH_Side = -100;
    public final int baseClawVal = 30;
    public final int magicNumOpen = 60;
    public double position = 0;//sets servo position to 0-1 multiplier
    public final double degree_mult = 0.00555555554;//100/180

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
    public int spot = 0;
    public double IN_distanceR = 0;//in distance for distance sensor 1
    public double IN_distanceL = 0;
    public double myMagic = 7;
    //color
    final float[] hsvValues = new float[3];//gets values for color sensor
    private int redVal = 0;//the red value in rgb
    private int greenVal = 0;//the green value in rgb
    private int blueVal = 0;//the blue value in rgb
    private String colorName = "N/A";//gets color name
    public NormalizedColorSensor colorSensorR;//declaring the colorSensor variable
    public NormalizedColorSensor colorSensorL;//declaring the colorSensor variable
    public TouchSensor touchSensor;
    public RevBlinkinLedDriver lights;
    private DigitalChannel red1;
    private DigitalChannel green1;
    private DigitalChannel red2;
    private DigitalChannel green2;
    private DigitalChannel red3;
    private DigitalChannel green3;
    private DigitalChannel red4;
    private DigitalChannel green4;
    private float redValR = 0;//the red value in rgb
    private float greenValR = 0;//the green value in rgb
    private float blueValR = 0;//the blue value in rgb
    private float redValL = 0;//the red value in rgb
    private float greenValL = 0;//the green value in rgb
    private float blueValL = 0;//the blue value in rgb

    @Override
    public void runOpMode() {
        lights = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        rDistance = hardwareMap.get(DistanceSensor.class, "rDistance");
        lDistance = hardwareMap.get(DistanceSensor.class, "lDistance");
        fDistance = hardwareMap.get(DistanceSensor.class, "fDistance");
        red1 = hardwareMap.get(DigitalChannel.class, "red1");
        green1 = hardwareMap.get(DigitalChannel.class, "green1");
        red2 = hardwareMap.get(DigitalChannel.class, "red2");
        green2 = hardwareMap.get(DigitalChannel.class, "green2");
        red3 = hardwareMap.get(DigitalChannel.class, "red3");
        green3 = hardwareMap.get(DigitalChannel.class, "green3");
        red4 = hardwareMap.get(DigitalChannel.class, "red4");
        green4 = hardwareMap.get(DigitalChannel.class, "green4");
        colorSensorR = hardwareMap.get(NormalizedColorSensor.class, "colorSensorR");
        colorSensorL = hardwareMap.get(NormalizedColorSensor.class, "colorSensorL");
        touchSensor = hardwareMap.get(TouchSensor.class, ("touchSensor"));

        motorFrontLeft = hardwareMap.get(DcMotor.class, "motorFrontLeft");
        motorBackLeft = hardwareMap.get(DcMotor.class, "motorBackLeft");
        motorFrontRight = hardwareMap.get(DcMotor.class, "motorFrontRight");
        motorBackRight = hardwareMap.get(DcMotor.class, "motorBackRight");
        deadWheel = hardwareMap.get(DcMotor.class, "deadWheel");
        //deadWheelL = hardwareMap.get(DcMotor.class, "deadWheelL");
        //deadWheelR = hardwareMap.get(DcMotor.class, "deadWheelR");
        clawServo = hardwareMap.get(Servo.class, "clawServo");
        sparkLong = hardwareMap.get(DcMotor.class, "sparkLong");

        //onInit();
        motorFrontRight.setDirection(DcMotor.Direction.REVERSE);
        motorBackRight.setDirection(DcMotor.Direction.REVERSE);

        resetEncoders();
        sparkLong.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        sparkLong.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        deadWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //deadWheelL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //deadWheelR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorBackRight.setZeroPowerBehavior(BRAKE);
        motorBackLeft.setZeroPowerBehavior(BRAKE);
        motorFrontRight.setZeroPowerBehavior(BRAKE);
        motorFrontLeft.setZeroPowerBehavior(BRAKE);
        red2.setMode(DigitalChannel.Mode.OUTPUT);
        green2.setMode(DigitalChannel.Mode.OUTPUT);
        red1.setMode(DigitalChannel.Mode.OUTPUT);
        green1.setMode(DigitalChannel.Mode.OUTPUT);
        red3.setMode(DigitalChannel.Mode.OUTPUT);
        green3.setMode(DigitalChannel.Mode.OUTPUT);
        red4.setMode(DigitalChannel.Mode.OUTPUT);
        green4.setMode(DigitalChannel.Mode.OUTPUT);

        telemetry.addData("Starting at", "%7d :%7d",
                motorBackRight.getCurrentPosition(),
                motorBackLeft.getCurrentPosition(),
                motorFrontLeft.getCurrentPosition());
        closeClaw();
        telemetry.update();
        lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
        closeClaw();
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        while (opModeIsActive()) {
            correctByColor();
            telemetry.update();
        }
    }

    public void correctByColor() {
        lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
        getAllColorR();
        getAllColorL();
        NormalizedRGBA colorsR = colorSensorR.getNormalizedColors();
        Color.colorToHSV(colorsR.toColor(), hsvValues);
        NormalizedRGBA colors = colorSensorL.getNormalizedColors();
        Color.colorToHSV(colors.toColor(), hsvValues);
        float redValR = colorsR.red;//the red value in rgb
        float greenValR = colorsR.green;//the green value in rgb
        float blueValR = colorsR.blue;//the blue value in rgb
        float redValL = colors.red;//the red value in rgb
        float greenValL = colors.green;//the green value in rgb
        float blueValL = colors.blue;//the blue value in rgb
        //right
        double redTargetRR = 0.003;//the red value in rgb
        double redTargetGR = 0.004;//the green value in rgb
        double redTargetBR = 0.003;//the blue value in rgb
        //left
        double redTargetRL = 0.003;//the red value in rgb
        double redTargetGL = 0.003;//the green value in rgb
        double redTargetBL = 0.002;//the blue value in rgb
        //right
        double blueTargetRR = 0.002;//the red value in rgb
        double blueTargetGR = 0.004;//the green value in rgb
        double blueTargetBR = 0.005;//the blue value in rgb
        //left
        double blueTargetRL = 0.001;//the red value in rgb
        double blueTargetGL = 0.003;//the green value in rgb
        double blueTargetBL = 0.0038;//the blue value in rgb
        double range = 0.0005;
        //left
        while (colorInRange(redValL, redTargetRL, greenValL, redTargetGL, blueValL, redTargetBL, (float) range)
                || colorInRange(redValL, blueTargetRL, greenValL, blueTargetGL, blueValL, blueTargetBL, (float) range)
                || colorInRange(redValR, redTargetRR, greenValR, redTargetGR, blueValR, redTargetBR, (float) range)
                || colorInRange(redValR, blueTargetRR, greenValR, blueTargetGR, blueValR, blueTargetBR, (float) range)) {
            if ((colorInRange(redValR, redTargetRR, greenValR, redTargetGR, blueValR, redTargetBR, (float) range)
                    || colorInRange(redValR, blueTargetRR, greenValR, blueTargetGR, blueValR, blueTargetBR, (float) range))) {
                getAllColorR();
                sideWaysEncoderDrive(1, 0.25, 0.4);//go left
                //right side has seen red or blue
            }
            if (colorInRange(redValL, redTargetRL, greenValL, redTargetGL, blueValL, redTargetBL, (float) range)
                    || colorInRange(redValL, blueTargetRL, greenValL, blueTargetGL, blueValL, blueTargetBL, (float) range)) {
                getAllColorL();
                sideWaysEncoderDrive(1, -0.25, 0.4);//go right
            }
            if (!colorInRange(redValL, redTargetRL, greenValL, redTargetGL, blueValL, redTargetBL, (float) range)
                    || !colorInRange(redValL, blueTargetRL, greenValL, blueTargetGL, blueValL, blueTargetBL, (float) range)
                    || !colorInRange(redValR, redTargetRR, greenValR, redTargetGR, blueValR, redTargetBR, (float) range)
                    || !colorInRange(redValR, blueTargetRR, greenValR, blueTargetGR, blueValR, blueTargetBR, (float) range)) {
                break;
            }
        }
        lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.valueOf(getColor()));
    }

    public boolean colorInRange(float red, double targetR, float green, double targetG, float blue, double targetB, float range) {
        boolean rCheck = false;
        boolean gCheck = false;
        boolean bCheck = false;
        if (targetR - range < red && red < targetR + range) {
            rCheck = true;
        }
        if (targetG - range < green && green < targetG + range) {
            gCheck = true;
        }
        if (targetB - range < blue && blue < targetB + range) {
            bCheck = true;
        }
        return rCheck && gCheck && bCheck;
    }

    public void getAllColorR() {
        //gives color values
        NormalizedRGBA colorsR = colorSensorR.getNormalizedColors();
        Color.colorToHSV(colorsR.toColor(), hsvValues);
        telemetry.addLine()
                .addData("Red", "%.3f", colorsR.red)
                .addData("Green", "%.3f", colorsR.green)
                .addData("Blue", "%.3f", colorsR.blue)
                .addData("Hue", "%.3f", hsvValues[0])
                .addData("Saturation", "%.3f", hsvValues[1])
                .addData("Value", "%.3f", hsvValues[2])
                .addData("Alpha", "%.3f", colorsR.alpha);
        telemetry.addLine()
                .addData("Color", colorName)
                .addData("RGB", "(" + redValR + "," + greenValR + "," + blueValR + ")");//shows rgb value
    }

    public void getAllColorL() {
        //gives color values
        NormalizedRGBA colors = colorSensorL.getNormalizedColors();
        Color.colorToHSV(colors.toColor(), hsvValues);
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
                .addData("RGB", "(" + redValL + "," + greenValL + "," + blueValL + ")");//shows rgb value
    }

    //precise if exact 180, if not, then use the following
    //final int actualF=50;
    //final int actualR=100;
    //final int actualL=44;
    //double myMagic2;
    //if ( fDistance.getDistance(DistanceUnit.INCH)<actualF){
    //    myMagic2=actualF-fDistance.getDistance(DistanceUnit.INCH);
    //    encoderDrive(0.75,-myMagic2,-myMagic2,3);
    //}
    //the following
    //while(fDistance.getDistance(DistanceUnit.INCH)<actualF){
    //    telemetry.addData("is working",fDistance.getDistance(DistanceUnit.INCH)<actualF);
    //    telemetry.addData("inches",fDistance.getDistance(DistanceUnit.INCH));
    //    telemetry.addData("actualF",actualF);
    //    telemetry.update();
    //    motorBackLeft.setPower(-0.8);
    //    motorBackRight.setPower(-0.8);
    //    motorFrontLeft.setPower(-0.8);
    //    motorFrontRight.setPower(-0.8);
    //}
    public String getColor() {
        final String[] favColors = {
                "RAINBOW_RAINBOW_PALETTE",
                "RAINBOW_PARTY_PALETTE",
                "BEATS_PER_MINUTE_RAINBOW_PALETTE",
                "BEATS_PER_MINUTE_PARTY_PALETTE",
                //"FIRE_MEDIUM",
                "COLOR_WAVES_RAINBOW_PALETTE",
                "COLOR_WAVES_PARTY_PALETTE",
                "CP2_END_TO_END_BLEND_TO_BLACK",
                "CP2_BREATH_SLOW",
                "CP1_2_END_TO_END_BLEND_1_TO_2",
                "CP1_2_END_TO_END_BLEND",
                "HOT_PINK",
                "GOLD",
                "VIOLET"
        };
        final int min = 0;
        final int max = favColors.length - 1;
        return favColors[(int) Math.floor(Math.random() * (max - min + 1) + min)];
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
