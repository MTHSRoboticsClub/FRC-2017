package Systems;

import com.ctre.CANTalon;
import NetworkComm.InputOutputComm;

//Chill Out 1778 class for controlling the drivetrain during auto

public class CANDriveAssembly {
	
	private static boolean initialized = false;
	
	// Speed controller IDs
	private static final int LEFT_FRONT_TALON_ID = 8;
	//private static final int LEFT_REAR_TALON_ID = 4;
	private static final int RIGHT_FRONT_TALON_ID = 3;
	//private static final int RIGHT_REAR_TALON_ID = 7;
			
	private static final double AUTO_DRIVE_ANGLE_CORRECT_COEFF = 0.02;
	private static final double GYRO_CORRECT_COEFF = 0.03;
		
	// speed controllers and drive class
	private static CANTalon mFrontLeft, mBackLeft, mFrontRight, mBackRight;
	
	// used as angle baseline (if we don't reset gyro)
	private static double initialAngle = 0.0;
	    	        
	// static initializer
	public static void initialize()
	{
		if (!initialized) {
	        mFrontLeft = new CANTalon(LEFT_FRONT_TALON_ID);
	        //mBackLeft = new CANTalon(LEFT_REAR_TALON_ID);
	        mFrontRight = new CANTalon(RIGHT_FRONT_TALON_ID);
	        //mBackRight = new CANTalon(RIGHT_REAR_TALON_ID);
	        	        	        	        	        
	        // initialize the NavXSensor
	        NavXSensor.initialize();
	        	        
	        initialized = true;
		}
	}


	public static void autoInit(boolean resetGyro) {
				
		if (resetGyro) {
			NavXSensor.reset();
			initialAngle = 0.0;
		}
		else
			initialAngle = NavXSensor.getAngle();
   	}
		
	private static double getGyroAngle() {
		
		//double gyroAngle = 0.0;
		//double gyroAngle = NavXSensor.getYaw();	  // -180 deg to +180 deg
		double gyroAngle = NavXSensor.getAngle();     // continuous angle (can be larger than 360 deg)
				
		//System.out.println("autoPeriodicStraight:  Gyro angle = " + gyroAngle);
			
		// send output data for test & debug
	    //InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Connected",NavXSensor.isConnected());
	    //InputOutputComm.putBoolean(InputOutputComm.LogTable.kMainLog,"Auto/IMU_Calibrating",NavXSensor.isCalibrating());

		String gyroAngleStr = String.format("%.2f", gyroAngle);
	    String myString = new String("gyroAngle = " + gyroAngleStr);
		//System.out.println(myString);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Auto/GyroAngle", myString);		

		return gyroAngle;
	}
			
	public static void autoPeriodicStraight(double speed) {
		// autonomous operation of drive straight - uses gyro
		
		double gyroAngle = getGyroAngle();
		
		// subtract the initial angle offset, if any
		gyroAngle -= initialAngle;
		
		// calculate speed adjustment for left and right sides (negative sign added as feedback)
		double driveAngle = -gyroAngle * AUTO_DRIVE_ANGLE_CORRECT_COEFF;
				
		double leftSpeed = speed+driveAngle;		
		double rightSpeed = speed-driveAngle;
				
		// adjust speed of left and right sides
		drive(leftSpeed, rightSpeed, 0.0);		 
	}

	public static void autoStop() {
		drive(0.0, 0.0, 0.0);
	}
		
	public static void teleopInit() {
	}
	
	public static void teleopPeriodic() {	
	}
	
	// CORE DRIVE METHOD
	// Assumes parameters are PercentVbus (0.0 to 1.0)
	public static void drive(double leftValue, double rightValue, double strafe) {
		
		double rightMotorPolarity = -1.0;  // right motor is inverted 
		double leftMotorPolarity = 1.0;    // left motor is not inverted

		String leftSpeedStr = String.format("%.2f", leftValue);
		String rightSpeedStr = String.format("%.2f", rightValue);
		String myString2 = new String("leftSpeed = " + leftSpeedStr + " rightSpeed = " + rightSpeedStr);
		//System.out.println(myString2);
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"Auto/AutoDrive", myString2);

		// set motor values directly
		mFrontLeft.set(leftMotorPolarity*leftValue);
		mFrontRight.set(rightMotorPolarity*rightValue);
	}
	
	public static void driveDirection(double angle, double speed) {
		double gyroAngle = getGyroAngle();	
		double driveAngle = (angle-gyroAngle)*GYRO_CORRECT_COEFF;
		drive(driveAngle+speed, -driveAngle+speed, 0);
	}
	
	public static void turnToDirection(double angle, double power) {
		double gyroAngle = getGyroAngle();
		double driveAngle = (angle-gyroAngle)*(1/360)*power;
		drive(driveAngle, -driveAngle, 0);
	}
	
	public static void driveForward(double forwardVel) {
		drive(forwardVel, forwardVel, 0);
	}
	
	public static void rotate(double angularVel) {
		drive(angularVel, -angularVel, 0);
	}
	
	public static void driveVelocity(double forwardVel, double angularVel) {
		drive((forwardVel+angularVel)/2.0,(forwardVel-angularVel)/2.0,0);
	}
		
	//Turn methods
	//===================================================
	public static void rotateLeft(double speed) {		
		drive(-speed, speed, 0);
	}

	public static void rotateRight(double speed) {
		drive(speed, -speed, 0);
	}
}
