package Systems;

import com.ctre.CANTalon;

import NetworkComm.InputOutputComm;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Utility;

public class BallManagement {
	
	private static boolean initialized = false;

	private static final int SHOOTER_TALON_ID = 6;
	private static final int CONVEYER_TALON_ID = 4;
	private static final int COLLECTOR_TALON_ID = 7;
	private static final int AGITATOR_TALON_ID = 5;
	
	private static final double CONVEYER_IN_LEVEL = 0.5;
	private static final double CONVEYER_OUT_LEVEL = -0.5;
	
	private static final double COLLECTOR_IN_LEVEL = 0.5;
	private static final double COLLECTOR_OUT_LEVEL = -0.5;
	
	private static final double DEAD_ZONE_THRESHOLD = 0.05;
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_VERY_LOW = 1;
	public static final int MOTOR_LOW = 2;
	public static final int MOTOR_MEDIUM = 3;
	public static final int MOTOR_HIGH = 4;
	public static final int MOTOR_VERY_HIGH = 5;
	public static final int MOTOR_MAX = 6;

	private static final double motorSettings[] = { 0.0, -0.1, -0.4, -0.6, -0.8, -0.9, -1.0 };
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	private static double currentMotorIndex = 0;
	
	private static CANTalon shooterMotor, conveyerMotor, collectorMotor, agitatorMotor;
	
	private static final int GAMEPAD_ID = 1;
	private static Joystick gamepad;
	
	// gamepad controls
	private static final int COLLECTOR_IN_AXIS = 2;
	private static final int COLLECTOR_OUT_BUTTON = 5;

	private static final int CONVEYER_IN_AXIS = 3;
	private static final int CONVEYER_OUT_BUTTON = 6;
	
	private static final int FIRE_HIGH_BUTTON = 2;
	private static final int FIRE_MEDIUM_BUTTON = 1;
	private static final int FIRE_LOW_BUTTON = 3;
	private static final int HOLD_BUTTON = 4;
	
	// wait 0.25 s between button pushes on shooter
    private static final int TRIGGER_CYCLE_WAIT_US = 250000;
    private static double initTriggerTime;
    
	public static void initialize() {
		if (initialized)
			return;

		// initialize motors
		shooterMotor = new CANTalon(SHOOTER_TALON_ID);
		conveyerMotor = new CANTalon(CONVEYER_TALON_ID);
		collectorMotor = new CANTalon(COLLECTOR_TALON_ID);
		agitatorMotor = new CANTalon(AGITATOR_TALON_ID);

		// make sure all motors are off
		resetMotors();
		
		gamepad = new Joystick(GAMEPAD_ID);
		
		initialized = true;
	}
	
	private static void resetMotors()
	{
		currentMotorIndex = MOTOR_OFF;
		shooterMotor.set(0);
		conveyerMotor.set(0);
		collectorMotor.set(0);
		agitatorMotor.set(0);		
	}
	
	private static void setShooterStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_MAX) || (newIndex < MOTOR_OFF))
			return;
		
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Teleop/ShooterLevel", motorSettings[newIndex]);		
		
		shooterMotor.set(motorSettings[newIndex]);	
	}
	
	public static void fireLevel(int motorLevel) {
		
		setShooterStrength(motorLevel);
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();
	}
		
	public static void teleopInit() {
		setShooterStrength(MOTOR_OFF);
        initTriggerTime = Utility.getFPGATime();
        
        // initialize the agitator (always on)
        double agitatorLevel = 0.3;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Teleop/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);
	}
	
	public static void teleopPeriodic() {
		
		// conveyer control
		double conveyerLevel = gamepad.getRawAxis(CONVEYER_IN_AXIS);
		if (Math.abs(conveyerLevel) > DEAD_ZONE_THRESHOLD)
			conveyerLevel = CONVEYER_IN_LEVEL;
		else if (gamepad.getRawButton(CONVEYER_OUT_BUTTON))
			conveyerLevel = CONVEYER_OUT_LEVEL;
		else
			conveyerLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Teleop/ConveyerLevel", conveyerLevel);		
		conveyerMotor.set(conveyerLevel);
		
		// collector control
		double collectorLevel = gamepad.getRawAxis(COLLECTOR_IN_AXIS);
		if (Math.abs(collectorLevel) > DEAD_ZONE_THRESHOLD)
			collectorLevel = COLLECTOR_IN_LEVEL;
		else if (gamepad.getRawButton(COLLECTOR_OUT_BUTTON))
			collectorLevel = COLLECTOR_OUT_LEVEL;
		else
			collectorLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Teleop/CollectorLevel", collectorLevel);
		collectorMotor.set(collectorLevel);

		// fire controls - using a timer to debounce
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;

		// shooter commands
		if (gamepad.getRawButton(FIRE_HIGH_BUTTON))
			fireLevel(MOTOR_HIGH);			
		
		if (gamepad.getRawButton(FIRE_MEDIUM_BUTTON))
			fireLevel(MOTOR_MEDIUM);			

		if (gamepad.getRawButton(FIRE_LOW_BUTTON)) 
			fireLevel(MOTOR_LOW);			
		
		if (gamepad.getRawButton(HOLD_BUTTON))
			fireLevel(MOTOR_OFF);
		
	}
	
}