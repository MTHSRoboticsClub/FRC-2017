package Systems;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.FeedbackDeviceStatus;
import com.ctre.CANTalon.TalonControlMode;

import NetworkComm.InputOutputComm;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Utility;

public class BallManagement {
	
	private static boolean initialized = false;

	private static final int COLLECTOR_RELAY_CHANNEL = 0;
	
	private static final int SHOOTER_TALON_ID = 6;
	private static final int AGITATOR_TALON_ID = 5;
	private static final int FEEDER_TALON_ID = 11;
	
	private static final int TRANSPORT_TALON_ID = 10;
	private static final int COLLECTOR_TALON_ID = 9;
	
	private static final double TRANSPORT_IN_LEVEL = 0.5;
	private static final double TRANSPORT_OUT_LEVEL = -0.5;
	
	private static final double COLLECTOR_IN_LEVEL = 0.5;
	private static final double COLLECTOR_OUT_LEVEL = -0.5;
	
	private static final double AGITATOR_LEVEL = 0.3;
	private static final double FEEDER_LEVEL = 0.3;
	
	//  10 100ms/s * (60 s/min) * (1 rev/12 Native Units)
	private static final double NATIVE_TO_RPM_FACTOR = 10 * 60 / 12;
	
	private static final double DEAD_ZONE_THRESHOLD = 0.05;
	
	public static final int MOTOR_OFF = 0;
	public static final int MOTOR_VERY_LOW = 1;
	public static final int MOTOR_LOW = 2;
	public static final int MOTOR_MEDIUM = 3;
	public static final int MOTOR_HIGH = 4;
	public static final int MOTOR_VERY_HIGH = 5;
	public static final int MOTOR_MAX = 6;

	private static final double motorSettings[] = { 0, 0, 100, 115, 130, 300, 300 };		    // Speed (Native) control settings
	//private static final double motorSettings[] = { 0.0, 0.1, 0.375, 0.43, 0.5, 1.0, 1.0 };   // Vbus (%) control settings
	private static final int NUM_MOTOR_SETTINGS = 7;
	
	private static Relay collectorRelay;
	
	// shooter and support motors
	private static CANTalon shooterMotor, feederMotor, agitatorMotor;
	
	// collector & transport motors
	private static CANTalon transportMotor, collectorMotor;
	
	private static final int GAMEPAD_ID = 1;
	private static Joystick gamepad;
		
	// gamepad controls
	private static final int COLLECTOR_IN_AXIS = 2;
	private static final int COLLECTOR_OUT_BUTTON = 5;

	private static final int TRANSPORT_IN_AXIS = 3;
	private static final int TRANSPORT_OUT_BUTTON = 6;
	
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
		
        // create and reset collector relay
        collectorRelay = new Relay(COLLECTOR_RELAY_CHANNEL,Relay.Direction.kForward);
        collectorRelay.set(Relay.Value.kOff);

		// create motors
		transportMotor = new CANTalon(TRANSPORT_TALON_ID);
		collectorMotor = new CANTalon(COLLECTOR_TALON_ID);
		
		feederMotor = new CANTalon(FEEDER_TALON_ID);
		agitatorMotor = new CANTalon(AGITATOR_TALON_ID);
		shooterMotor = new CANTalon(SHOOTER_TALON_ID);
		
		// set up shooter motor sensor
		shooterMotor.reverseSensor(true);
		shooterMotor.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//shooterMotor.configEncoderCodesPerRev(12);   // do not use this unless you want RPM-ish values!
		
		// USE FOR DEBUG ONLY:  configure shooter motor for open loop speed control
		//shooterMotor.changeControlMode(TalonControlMode.PercentVbus);
		
		// configure shooter motor for closed loop speed control
		shooterMotor.changeControlMode(TalonControlMode.Speed);
		shooterMotor.configNominalOutputVoltage(+0.0f, -0.0f);
		shooterMotor.configPeakOutputVoltage(+12.0f, -12.0f);
		shooterMotor.setProfile(0);
		shooterMotor.setP(9.0);
		shooterMotor.setI(0.001);
		shooterMotor.setD(0.8);
		shooterMotor.setF(4.35);

		// make sure all motors are off
		resetMotors();
		
		gamepad = new Joystick(GAMEPAD_ID);
		
		initialized = true;
	}
	
	public static void resetMotors()
	{		
		shooterMotor.set(0);
		feederMotor.set(0);
		agitatorMotor.set(0);
		
		transportMotor.set(0);
		collectorMotor.set(0);
		
	}
	
	public static void setShooterStrength(int newIndex) {
		
		if (!initialized)
			initialize();
		
		// if out of range, just return
		if ((newIndex > MOTOR_MAX) || (newIndex < MOTOR_OFF))
			return;
		
		if (newIndex == MOTOR_OFF) {
			stopFeeding();  // turn off feeder motors
		}
		
		//System.out.println("Motor Strength = " + motorSettings[newIndex]);
		double shooter_rpm = motorSettings[newIndex] * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Target", shooter_rpm);		
		
		shooterMotor.set(motorSettings[newIndex]);	
		
		// if shooter is on, make sure agitator and feeder are on AFTER shooter is turned on
		if (newIndex != MOTOR_OFF)
			startFeeding();
		
		// reset trigger init time
		initTriggerTime = Utility.getFPGATime();		
	}
	
	public static void startFeeding() {
        double agitatorLevel = AGITATOR_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);		
        
        double feederLevel = FEEDER_LEVEL;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);		
	}
	
	public static void stopFeeding() {
        double agitatorLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/AgitatorLevel", agitatorLevel);		
        agitatorMotor.set(agitatorLevel);		
        
        double feederLevel = 0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/FeederLevel", feederLevel);		
        feederMotor.set(feederLevel);		
	}		
	
	private static void checkCollectorControls() {
		
		// transprt control
		double transportLevel = gamepad.getRawAxis(TRANSPORT_IN_AXIS);
		if (Math.abs(transportLevel) > DEAD_ZONE_THRESHOLD)
			transportLevel = TRANSPORT_IN_LEVEL;
		else if (gamepad.getRawButton(TRANSPORT_OUT_BUTTON))
			transportLevel = TRANSPORT_OUT_LEVEL;
		else
			transportLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/TransportLevel", transportLevel);		
		transportMotor.set(transportLevel);
		
		// collector control
		double collectorLevel = gamepad.getRawAxis(COLLECTOR_IN_AXIS);
		if (Math.abs(collectorLevel) > DEAD_ZONE_THRESHOLD)
			collectorLevel = COLLECTOR_IN_LEVEL;
		else if (gamepad.getRawButton(COLLECTOR_OUT_BUTTON))
			collectorLevel = COLLECTOR_OUT_LEVEL;
		else
			collectorLevel = 0.0;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/CollectorLevel", collectorLevel);
		collectorMotor.set(collectorLevel);
		
	}
	
	private static void checkShooterControls() {
		// fire controls - using a timer to debounce
		double currentTime = Utility.getFPGATime();

		// if not enough time has passed, no polling allowed!
		if ((currentTime - initTriggerTime) < TRIGGER_CYCLE_WAIT_US)
			return;

		// shooter commands
		if (gamepad.getRawButton(FIRE_HIGH_BUTTON))
			setShooterStrength(MOTOR_HIGH);			
		
		if (gamepad.getRawButton(FIRE_MEDIUM_BUTTON))
			setShooterStrength(MOTOR_MEDIUM);			

		if (gamepad.getRawButton(FIRE_LOW_BUTTON))
			setShooterStrength(MOTOR_LOW);			
		
		if (gamepad.getRawButton(HOLD_BUTTON))
			setShooterStrength(MOTOR_OFF);
		
	}
			
	public static void teleopInit() {
		// turn on relay
    	collectorRelay.set(Relay.Value.kOn);
				
		resetMotors();
		
        initTriggerTime = Utility.getFPGATime();
        
	}
	
	public static void teleopPeriodic() {
		
		checkCollectorControls();
		checkShooterControls();
		
		// DEBUG - report on shooter motor values		
		double speed_rpm = shooterMotor.getSpeed() * NATIVE_TO_RPM_FACTOR;
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/ShooterRpm_Actual", speed_rpm);
		
		//double encVelocity = shooterMotor.getEncVelocity();
		//InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/encVelocity", encVelocity);
				
		double motorOutput = shooterMotor.getOutputVoltage()/shooterMotor.getBusVoltage();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/motorOutput", motorOutput);

		double closedLoopError = shooterMotor.getClosedLoopError();
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"BallMgmt/closedLoopError", closedLoopError);
		
	}
	
}
