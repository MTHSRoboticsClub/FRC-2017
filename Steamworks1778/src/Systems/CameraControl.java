package Systems;

import NetworkComm.InputOutputComm;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;

public class CameraControl {
	private static boolean initialized = false;
	
	private static final int DRIVER_CONTROL_ID = 0;
	
	private static final int SERVO_CHANNEL_ID = 0;
	private static final int CAMERA_CONTROL_BUTTON = 1;
	
	private static final int CAMERA_LED_RELAY_CHANNEL = 2;
	
	private static Joystick gamepad;
	
	// assumes HS-475HB servo, which 1.0 = 90 degrees
	public static final double BOILER_CAM_POS = 0.125;  // 11.25 deg
	public static final double GEAR_CAM_POS = 0.0;     // 0 deg
	private static final double SERVO_POS_TOLERANCE = 0.005;
	
	// Relay for extra LEDs
	private static Relay cameraLedRelay;
	
	// camera position servo
	private static Servo positionServo;
	
	public static void initialize() {
		if (initialized)
			return;
		
		cameraLedRelay = new Relay(CAMERA_LED_RELAY_CHANNEL,Relay.Direction.kForward);
		cameraLedRelay.set(Relay.Value.kOff);
		
		positionServo = new Servo(SERVO_CHANNEL_ID);
		
		gamepad = new Joystick(DRIVER_CONTROL_ID);
		
		initialized = true;
	}
	
	public static void moveToPos(double position) {
		if ((position < GEAR_CAM_POS) || (position > BOILER_CAM_POS))
			return;
		
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"CameraControl/Angle", position*90.0);
		
		positionServo.set(position);
	}
	
	public static void setCameraLed(boolean state) {
		if (state == true) {
			cameraLedRelay.set(Relay.Value.kOn);
		}
		else {
			cameraLedRelay.set(Relay.Value.kOff);			
		}
	}
	
	public static void teleopInit() {
		
		// turn off extra LEDs for teleop (not needed)
		setCameraLed(false);
	}
	
	public static void teleopPeriodic() {
		double currentPos = positionServo.get();
		
		if (gamepad.getRawButton(CAMERA_CONTROL_BUTTON) == true)
		{
			if (Math.abs(currentPos - GEAR_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(GEAR_CAM_POS);
		}
		else
		{
			if (Math.abs(currentPos - BOILER_CAM_POS) > SERVO_POS_TOLERANCE)
				moveToPos(BOILER_CAM_POS);
		}
			
	}
}
