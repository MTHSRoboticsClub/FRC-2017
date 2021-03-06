package FreezyDrive;

/** 
    Use this class to map the buttons and tumbsticks of the Logitech controllers to named uses.
	For reference, all the button IDs are listed in the LogitechF310.java file.
*/
import edu.wpi.first.wpilibj.Joystick;
import Utility.InterLinkElite;
import Utility.LogitechF310;


public class Controller {

	// Types: 0 = Logitech F310, 1 = InterLink Flight Controller, 2 = Logitech Dual Action
	public static final int DRIVER_CONTROLLER_TYPE = 1;
    //public static final int COPILOT_CONTROLLER_TYPE = 1;
    public static final int PORT_DRIVER_CONTROLLER = 0;
    //public static final int PORT_COPILOT_CONTROLLER = 1;
	public static Joystick Driver;
	public static Joystick CoPilot;
	
	public static boolean[] controllerType = new boolean[3];
	
	public static boolean initialized = false;
	    
    public static void initialize() {
    	
    	if (initialized)
    		return;
    	
    	Driver = new Joystick(PORT_DRIVER_CONTROLLER);
    	//CoPilot = new Joystick(PORT_COPILOT_CONTROLLER);
   	
    	initialized = true;
    }
    
    public static double Driver_Throttle (){
		return Driver.getRawAxis(LogitechF310.Axis.LEFT_Y);
    }

    public static double Driver_Steering (){
    	return -Driver.getRawAxis(LogitechF310.Axis.RIGHT_X);
    }
    
    public static boolean Driver_isQuickTurn(){
    	return !Driver.getRawButton(LogitechF310.RB);
    }
    
    public static boolean Driver_isShooting(){
    	if (LogitechF310.Axis.LT > .9 && LogitechF310.Axis.RT > .9)
    		return true;
    	else
    		return false;
    				
    }
    
}