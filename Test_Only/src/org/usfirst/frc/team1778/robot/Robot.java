package org.usfirst.frc.team1778.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import networkComm.InputOutputComm;
import drive.Controller;


public class Robot extends IterativeRobot {
	
	@Override
	public void robotInit() {
		
		// Start up network tables
		drive.Controller.initialize();
		InputOutputComm.initialize();
		InputOutputComm.putString(InputOutputComm.LogTable.kMainLog,"MainLog","robot initialized...");
		
	}

	
	@Override
	public void autonomousInit() {
		
	}

	
	@Override
	public void autonomousPeriodic() {
		
	}

	
	@Override
	public void teleopPeriodic() {
		InputOutputComm.putDouble(InputOutputComm.LogTable.kMainLog,"Controller Value X", Controller.Driver_Throttle());
	}

	
	@Override
	public void testPeriodic() {
		
	}
}

