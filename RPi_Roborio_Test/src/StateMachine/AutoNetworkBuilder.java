package StateMachine;

import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AutoNetworkBuilder {
		
	private final static String PREF_ROOT = "ChillOutAutonomousNetworks";
	private static Preferences prefRoot, prefs;
	
	private static ArrayList<AutoNetwork> autoNets;
	
	private static boolean initialized = false;
		
	public static void initialize() throws Exception {
		
		if (!initialized) {
			autoNets = null;
			prefRoot = Preferences.userRoot();
			prefs = prefRoot.node(PREF_ROOT);
			
			initialized = true;
		}
	}
	
	public static ArrayList<AutoNetwork> readInNetworks() {
		
		try {
			if (!initialized)
				initialize();
			}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		autoNets = new ArrayList<AutoNetwork>();
			
		/***** use only when storing the preferences first time *****/
		
		// clear current preferences keys from previous runs
		try {
			prefs.clear();
			Preferences node = prefs.node("<Do Nothing Network>");
			node.removeNode();
		}
		catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
		// create networks
		autoNets.add(AutoChooser.DO_NOTHING, createDoNothingNetwork());	
		autoNets.add(AutoChooser.DRIVE_FORWARD, createDriveForward());	
		autoNets.add(AutoChooser.DEPOSIT_GEAR_LEFT, createDepositGearLeft());	
		autoNets.add(AutoChooser.DEPOSIT_GEAR_CENTER, createDepositGearCenter());	
		autoNets.add(AutoChooser.DEPOSIT_GEAR_RIGHT, createDepositGearRight());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_BLUE_LEFT, createDriveAndShootBlueLeft());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_BLUE_CENTER, createDriveAndShootBlueCenter());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_BLUE_RIGHT, createDriveAndShootBlueRight());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_RED_LEFT, createDriveAndShootRedLeft());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_RED_CENTER, createDriveAndShootRedCenter());	
		autoNets.add(AutoChooser.DRIVE_AND_SHOOT_RED_RIGHT, createDriveAndShootRedRight());	
		
		// add the networks to the prefs object
		int counter = 0;
		for (AutoNetwork a: autoNets)
			a.persistWrite(counter++,prefs);		
				
		// store networks to file
	    try {
	        FileOutputStream fos = new FileOutputStream("/home/lvuser/chillOutAutoNets.xml");
	        prefs.exportSubtree(fos);
	        fos.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	   
		
		/**** TODO: normal operation - read in preferences file ***/
		
		return autoNets;
	}
	
	private void parseSingleNetwork() {
		
	}
		
	// **** DO NOTHING Network ***** 
	private static AutoNetwork createDoNothingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Do Nothing Network>");
		
		AutoState idleState = new AutoState("<Idle State>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState.addAction(deadEnd);

		autoNet.addState(idleState);	
		
		return autoNet;
	}

	// **** MOVE FORWARD Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) go back to idle and stay there 
	private static AutoNetwork createDriveForward() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}

	// **** DEPOSIT GEAR LEFT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) drive forward, using camera feedback to steer then stop
	// 5) go back to idle and stay there 
	private static AutoNetwork createDepositGearLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (left side) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +45 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",45, true, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(45, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
		
		AutoState driveToTargetState = new AutoState("<Drive To Target State 1>");
		DriveTowardTargetAction driveToTarget = new DriveTowardTargetAction("<Drive To Target Action>", 0.5, 80, 90); // desired target at x=80, y=90 (assume 160x120 img)
		UltrasonicEvent ultra1 = new UltrasonicEvent(2.0);  // ultrasonic event triggers at 2 inches
		driveToTargetState.addAction(driveToTarget);
		driveToTargetState.addEvent(ultra1);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(driveToTargetState);
		driveToTargetState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(driveToTargetState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}
	
	// **** DEPOSIT GEAR CENTER Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) drive forward, using camera feedback to steer then stop
	// 4) go back to idle and stay there 
	private static AutoNetwork createDepositGearCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (Center) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
				
		AutoState driveToTargetState = new AutoState("<Drive To Target State 1>");
		DriveTowardTargetAction driveToTarget = new DriveTowardTargetAction("<Drive To Target Action>", 0.5, 80, 90);   // desired target at x=80, y=90 (assume 160x120 img)
		UltrasonicEvent ultra1 = new UltrasonicEvent(2.0);  // ultrasonic event triggers at 2 inches
		driveToTargetState.addAction(driveToTarget);
		driveToTargetState.addEvent(ultra1);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(driveToTargetState);
		driveToTargetState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(driveToTargetState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}

	// **** DEPOSIT GEAR RIGHT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) drive forward, using camera feedback to steer then stop
	// 5) go back to idle and stay there 
	private static AutoNetwork createDepositGearRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Deposit Gear (Right Side) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -45 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-45, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-45, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
		
		AutoState driveToTargetState = new AutoState("<Drive To Target State 1>");
		DriveTowardTargetAction driveToTarget = new DriveTowardTargetAction("<Drive To Target Action>", 0.5, 80, 90); // desired target at x=80, y=90 (assume 160x120 img)
		UltrasonicEvent ultra1 = new UltrasonicEvent(2.0);  // ultrasonic event triggers at 2 inches
		driveToTargetState.addAction(driveToTarget);
		driveToTargetState.addEvent(ultra1);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveToTargetState);
		driveToTargetState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveToTargetState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
	
	// **** DRIVE AND SHOOT BLUE LEFT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) Calibrate shooter
	// 5) Shoot at high goal
	// 6) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootBlueLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Blue Left Side) Network>");
				
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -135 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-135, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-135, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 30);  // desired target at x=80, y=30 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 30, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
	
	// **** DRIVE AND SHOOT BLUE CENTER Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) drive forward a number of sec
	// 5) Calibrate shooter
	// 6) Shoot at high goal
	// 7) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootBlueCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Blue Center) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -120 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-120, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-120, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 40);  // desired target at x=80, y=40 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 40, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}

	// **** DRIVE AND SHOOT BLUE RIGHT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn LEFT a number of degrees
	// 4) drive forward a number of sec
	// 5) Calibrate shooter
	// 6) Shoot at high goal
	// 7) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootBlueRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Blue Right Side) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -110 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-110, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-110, true, GyroAngleEvent.AnglePolarity.kLessThan);
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 50);  // desired target at x=80, y=50 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 50, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnLeftState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}

	// **** DRIVE AND SHOOT RED LEFT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) drive forward a number of sec
	// 5) Calibrate shooter
	// 6) Shoot at high goal
	// 7) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootRedLeft() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Red Left Side) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +110 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn Right action>",110, true, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(110, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 50); // desired target at x=80, y=50 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 50, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
	
	// **** DRIVE AND SHOOT RED CENTER Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) drive forward a number of sec
	// 5) Calibrate shooter
	// 6) Shoot at high goal
	// 7) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootRedCenter() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Red Center) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +120 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn Right action>",120, true, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(120, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 40); // desired target at x=80, y=40 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 40, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}

	// **** DRIVE AND SHOOT RED RIGHT SIDE Network ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn RIGHT a number of degrees
	// 4) drive forward a number of sec
	// 5) Calibrate shooter
	// 6) Shoot at high goal
	// 7) go back to idle and stay there 
	private static AutoNetwork createDriveAndShootRedRight() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive and Shoot (Red Right Side) Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action>", 0.5, true);
		TimeEvent timer2 = new TimeEvent(1.0);  // drive forward timer event
		driveState.addAction(driveForward);
		driveState.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +135 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn Right action>",135, true, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(135, true, GyroAngleEvent.AnglePolarity.kGreaterThan);
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState targetCalState = new AutoState("<Cal Target State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>", 80, 30); // desired target at x=80, y=30 (assume 160x120 img)
		CalibratedEvent calEvent1 = new CalibratedEvent(80, 30, 5, 5);
		targetCalState.addAction(calTarget);
		targetCalState.addEvent(calEvent1);
		
		// TODO:  Shooter state here

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(turnRightState);
		turnRightState.associateNextState(targetCalState);
		targetCalState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(turnRightState);
		autoNet.addState(targetCalState);
		autoNet.addState(idleState2);
		
		return autoNet;
	}
	
	
	/*****************************************************************************************/
	/**** LEGACY NETWORKS **** Networks below this are for reference only and are not used ***/
	/*****************************************************************************************/
	
	// ****  [FOLLOW TARGET] Network - mainly for autotargeting testing - does not shoot ***** 
	// 1) be idle for a number of sec
	// 2) calibrate shooter continuously!  Never stop following target!  NEVER!
	private static AutoNetwork createTargetFollowerNetwork() {

		AutoNetwork autoNet = new AutoNetwork("<Target Follower Network>");

		// create states
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction idleStart = new IdleAction("<Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(idleStart);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);
		
		AutoState targetCalState = new AutoState("<Cal Target FOREVER State 1>");
		CalibrateTargetAction calTarget = new CalibrateTargetAction("<Cal Target Action 1>",80, 60);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		targetCalState.addAction(calTarget);
		targetCalState.addAction(doSomething4);
		targetCalState.addAction(doSomething5);
		
		// connect each state with a state to move to
		idleState.associateNextState(targetCalState);
						
		autoNet.addState(idleState);
		autoNet.addState(targetCalState);
		
		return autoNet;
	}

	// **** MOVE FORWARD Network - slow and steady ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) go back to idle and stay there 
	private static AutoNetwork createDriveForwardNetwork_Slow() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network - Slow>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action - Slow>", 0.5, true);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		TimeEvent timer2 = new TimeEvent(3.0);  // drive forward timer event
		driveState.addAction(driveForward);
		idleState.addAction(doSomething4);
		idleState.addAction(doSomething5);
		driveState.addEvent(timer2);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
		driveState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}
	
	// **** MOVE FORWARD FOREVER Network - slow and steady ***** 
	// 1) be idle for a number of sec
	// 2) drive forward forever (never stop)
	private static AutoNetwork createDriveForwardForeverNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Drive Forward Network - Slow>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);

		AutoState driveState = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward = new DriveForwardAction("<Drive Forward Action - Slow>", 0.5, true);
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		driveState.addAction(driveForward);
		idleState.addAction(doSomething4);
		idleState.addAction(doSomething5);
						
		// connect each event with a state to move to
		idleState.associateNextState(driveState);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState);
				
		return autoNet;
	}
	
	// **** COMPLEX DRIVING Network - drive in L pattern and return to original spot ***** 
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec X
	// 3) Turn 90 deg left
	// 4) drive forward for a number of sec Y
	// 5) Turn around (180 deg right)
	// 6) drive forward for a number of sec Y
	// 7) Turn 90 deg right
	// 8) drive forward for a number of sec X
	// 9) Turn around (180 deg left)
	// 10) go back to idle and stay there 
	private static AutoNetwork createComplexDrivingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Complex Driving Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.75, true);
		TimeEvent timer2 = new TimeEvent(1.5);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -90 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-90, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-90, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
			
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.75, true);
		TimeEvent timer3 = new TimeEvent(1.5);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer3);
		
		AutoState aboutFaceRightState = new AutoState("<About Face Right State +180 deg>");
		TurnAction aboutFaceRightAction = new TurnAction("<About Face right action>",180, true, 0.5);
		GyroAngleEvent gyroAboutFaceRight = new GyroAngleEvent(180, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +180 deg
		aboutFaceRightState.addAction(aboutFaceRightAction);
		aboutFaceRightState.addEvent(gyroAboutFaceRight);

		AutoState driveState3 = new AutoState("<Drive State 3>");
		DriveForwardAction driveForward3 = new DriveForwardAction("<Drive Forward Action 3>", 0.75, true);
		TimeEvent timer4 = new TimeEvent(1.5);  // drive forward timer event
		driveState3.addAction(driveForward3);
		driveState3.addEvent(timer4);

		AutoState turnRightState = new AutoState("<Turn Right State +90 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",90, true, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(90, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState driveState4 = new AutoState("<Drive State 4>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4>", 0.75, true);
		TimeEvent timer5 = new TimeEvent(1.5);  // drive forward timer event
		driveState4.addAction(driveForward4);
		driveState4.addEvent(timer5);

		AutoState aboutFaceLeftState = new AutoState("<About Face Left State -180 deg>");
		TurnAction aboutFaceLeftAction = new TurnAction("<About Face left action>",-180, true, 0.5);
		GyroAngleEvent gyroAboutFaceLeft = new GyroAngleEvent(-180, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -180 deg
		aboutFaceLeftState.addAction(aboutFaceLeftAction);
		aboutFaceLeftState.addEvent(gyroAboutFaceLeft);

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(aboutFaceRightState);
		aboutFaceRightState.associateNextState(driveState3);
		driveState3.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState4);
		driveState4.associateNextState(aboutFaceLeftState);
		aboutFaceLeftState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(aboutFaceRightState);
		autoNet.addState(driveState3);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState4);
		autoNet.addState(aboutFaceLeftState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}

	// **** ABSOLUTE COMPLEX DRIVING Network - drive in L pattern and return to original spot ***** 
	// Uses absolute angle headings instead of relative angles between states
	// Note: this network does NOT reset the gyro!!
	//
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec X
	// 3) Turn to -90 deg heading
	// 4) drive forward for a number of sec Y
	// 5) Turn to +90 deg heading
	// 6) drive forward for a number of sec Y
	// 7) Turn to +180 deg heading
	// 8) drive forward for a number of sec X
	// 9) Turn to 0 deg heading
	// 10) go back to idle and stay there 
	private static AutoNetwork createAbsoluteComplexDrivingNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Absolute Complex Driving Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);

		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.75, false);
		TimeEvent timer2 = new TimeEvent(1.5);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);
		
		AutoState turnLeftState = new AutoState("<Turn to -90 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn to -90 deg action>",-90, false, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-90, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
			
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.75, false);
		TimeEvent timer3 = new TimeEvent(1.5);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer3);
		
		AutoState aboutFaceRightState = new AutoState("<Turn to 90 deg>");
		TurnAction aboutFaceRightAction = new TurnAction("<Turn to 90 deg action>",90, false, 0.5);
		GyroAngleEvent gyroAboutFaceRight = new GyroAngleEvent(90, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +180 deg
		aboutFaceRightState.addAction(aboutFaceRightAction);
		aboutFaceRightState.addEvent(gyroAboutFaceRight);

		AutoState driveState3 = new AutoState("<Drive State 3>");
		DriveForwardAction driveForward3 = new DriveForwardAction("<Drive Forward Action 3>", 0.75, false);
		TimeEvent timer4 = new TimeEvent(1.5);  // drive forward timer event
		driveState3.addAction(driveForward3);
		driveState3.addEvent(timer4);

		AutoState turnRightState = new AutoState("<Turn to 180 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn to 180 deg action>",180, false, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(180, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState driveState4 = new AutoState("<Drive State 4>");
		DriveForwardAction driveForward4 = new DriveForwardAction("<Drive Forward Action 4>", 0.75, false);
		TimeEvent timer5 = new TimeEvent(1.5);  // drive forward timer event
		driveState4.addAction(driveForward4);
		driveState4.addEvent(timer5);

		AutoState aboutFaceLeftState = new AutoState("<Turn to 0 deg>");
		TurnAction aboutFaceLeftAction = new TurnAction("<Turn to 0 deg action>",0, false, 0.5);
		GyroAngleEvent gyroAboutFaceLeft = new GyroAngleEvent(0, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -180 deg
		aboutFaceLeftState.addAction(aboutFaceLeftAction);
		aboutFaceLeftState.addEvent(gyroAboutFaceLeft);

		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState2.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState2);
		driveState2.associateNextState(aboutFaceRightState);
		aboutFaceRightState.associateNextState(driveState3);
		driveState3.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState4);
		driveState4.associateNextState(aboutFaceLeftState);
		aboutFaceLeftState.associateNextState(idleState2);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnLeftState);
		autoNet.addState(driveState2);
		autoNet.addState(aboutFaceRightState);
		autoNet.addState(driveState3);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState4);
		autoNet.addState(aboutFaceLeftState);
		autoNet.addState(idleState2);
				
		return autoNet;
	}

	// **** Pacing Forever Network - Pace back and forth forever ***** 
	// This network uses absolute headings, and does NOT reset the gyro!
	//
	// 1) be idle for a number of sec
	// 2) drive forward for a number of sec
	// 3) Turn to 180 deg heading
	// 4) drive forward for a number of sec
	// 5) Turn to 0 deg heading
	// 6) Go back to state 2
	private static AutoNetwork createPacingForeverNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Pacing Forever Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);
		
		AutoState driveState1 = new AutoState("<Drive State 1>");
		DriveForwardAction driveForward1 = new DriveForwardAction("<Drive Forward Action 1>", 0.5, false);
		TimeEvent timer2 = new TimeEvent(2.0);  // drive forward timer event
		driveState1.addAction(driveForward1);
		driveState1.addEvent(timer2);

		AutoState turnRightState = new AutoState("<Turn to 180 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn to 180 deg action>", 180, false, 0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(180, false, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for -90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);
	
		AutoState driveState2 = new AutoState("<Drive State 2>");
		DriveForwardAction driveForward2 = new DriveForwardAction("<Drive Forward Action 2>", 0.5, false);
		TimeEvent timer3 = new TimeEvent(2.0);  // drive forward timer event
		driveState2.addAction(driveForward2);
		driveState2.addEvent(timer3);
		
		AutoState turnLeftState = new AutoState("<Turn to 0 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn to 0 deg action>", 0, false, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(0, false, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for +90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
				
		// connect each event with a state to move to
		// last state loops back!
		idleState.associateNextState(driveState1);
		driveState1.associateNextState(turnRightState);
		turnRightState.associateNextState(driveState2);
		driveState2.associateNextState(turnLeftState);
		turnLeftState.associateNextState(driveState1);
						
		autoNet.addState(idleState);
		autoNet.addState(driveState1);
		autoNet.addState(turnRightState);
		autoNet.addState(driveState2);
		autoNet.addState(turnLeftState);
				
		return autoNet;
	}

	
	// **** SPINNY Network - Spin X revolutions in one direction, pause, Spin X revolutions back ***** 
	// 1) be idle for a number of sec
	// 2) Turn 1080 deg left (three turns)
	// 3) be idle for a number of sec
	// 4) Turn 1080 deg right (three turns)
	// 5) go back to idle and stay there 
	private static AutoNetwork createSpinnyNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Spinny Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		TimeEvent timer1 = new TimeEvent(0.5);  // timer event
		idleState.addAction(startIdle);
		idleState.addEvent(timer1);
		
		AutoState turnLeftState = new AutoState("<Turn Left State -1080 deg>");
		TurnAction turnLeftAction = new TurnAction("<Turn left action>",-1080, true, 0.5);
		GyroAngleEvent gyroLeft = new GyroAngleEvent(-1080, true, GyroAngleEvent.AnglePolarity.kLessThan);  // gyro angle event for -90 deg
		turnLeftState.addAction(turnLeftAction);
		turnLeftState.addEvent(gyroLeft);
	
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction pauseAction = new IdleAction("<Pause Action>");
		TimeEvent timer2 = new TimeEvent(5.0);  // timer event
		idleState2.addAction(pauseAction);
		idleState2.addEvent(timer2);
		
		AutoState turnRightState = new AutoState("<Turn Right State +1080 deg>");
		TurnAction turnRightAction = new TurnAction("<Turn right action>",1080, true,  0.5);
		GyroAngleEvent gyroRight = new GyroAngleEvent(1080, true, GyroAngleEvent.AnglePolarity.kGreaterThan);  // gyro angle event for +90 deg
		turnRightState.addAction(turnRightAction);
		turnRightState.addEvent(gyroRight);

		AutoState idleState3 = new AutoState("<Idle State 3>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState3.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(turnLeftState);
		turnLeftState.associateNextState(idleState2);
		idleState2.associateNextState(turnRightState);
		turnRightState.associateNextState(idleState3);
						
		autoNet.addState(idleState);
		autoNet.addState(turnLeftState);
		autoNet.addState(idleState2);
		autoNet.addState(turnRightState);
		autoNet.addState(idleState3);
				
		return autoNet;
	}
	
	// **** Test Network - does nothing except transitions states ***** 
	private static AutoNetwork createTestNetwork() {
		
		AutoNetwork autoNet = new AutoNetwork("<Test Network>");
		
		AutoState idleState = new AutoState("<Idle State 1>");
		IdleAction startIdle = new IdleAction("<Start Idle Action 1>");
		IdleAction doSomething2 = new IdleAction("<Placeholder Action 2>");
		IdleAction doSomething3 = new IdleAction("<Placeholder Action 3>");
		TimeEvent timer1 = new TimeEvent(10.0);  // timer event
		idleState.addAction(startIdle);
		idleState.addAction(doSomething2);
		idleState.addAction(doSomething3);
		idleState.addEvent(timer1);
		
		AutoState idleState2 = new AutoState("<Idle State 2>");
		IdleAction startIdle2 = new IdleAction("<Start Idle Action 2>");
		IdleAction doSomething4 = new IdleAction("<Placeholder Action 4>");
		IdleAction doSomething5 = new IdleAction("<Placeholder Action 5>");
		TimeEvent timer2 = new TimeEvent(10.0);  // timer event
		idleState2.addAction(startIdle2);
		idleState2.addAction(doSomething4);
		idleState2.addAction(doSomething5);
		idleState2.addEvent(timer2);
		
		AutoState idleState3 = new AutoState("<Idle State 3>");
		IdleAction startIdle3 = new IdleAction("<Start Idle Action 3>");
		IdleAction doSomething6 = new IdleAction("<Placeholder Action 6>");
		IdleAction doSomething7 = new IdleAction("<Placeholder Action 7>");
		TimeEvent timer3 = new TimeEvent(10.0);  // timer event
		idleState3.addAction(startIdle3);
		idleState3.addAction(doSomething6);
		idleState3.addAction(doSomething7);
		idleState3.addEvent(timer3);
		
		AutoState idleState4 = new AutoState("<Idle State 4>");
		IdleAction deadEnd = new IdleAction("<Dead End Action>");
		idleState4.addAction(deadEnd);
				
		// connect each event with a state to move to
		idleState.associateNextState(idleState2);
		idleState2.associateNextState(idleState3);
		idleState3.associateNextState(idleState4);
						
		autoNet.addState(idleState);
		autoNet.addState(idleState2);
		autoNet.addState(idleState3);
		autoNet.addState(idleState4);
				
		return autoNet;
	}

	
}
