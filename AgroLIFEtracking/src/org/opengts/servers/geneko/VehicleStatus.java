package org.opengts.servers.geneko;

import org.opengts.db.StatusCodes;
import org.opengts.util.Payload;
import org.opengts.util.Print;

public class VehicleStatus {

	/* GTS status codes for Input-On events */
    private static final int InputStatusCodes_ON[] = new int[] {
        StatusCodes.STATUS_INPUT_ON_00,
        StatusCodes.STATUS_INPUT_ON_01,
        StatusCodes.STATUS_INPUT_ON_02,
        StatusCodes.STATUS_INPUT_ON_03,
        StatusCodes.STATUS_INPUT_ON_04,
        StatusCodes.STATUS_INPUT_ON_05,
        StatusCodes.STATUS_INPUT_ON_06,
        StatusCodes.STATUS_INPUT_ON_07,
        StatusCodes.STATUS_INPUT_ON_08,
        StatusCodes.STATUS_INPUT_ON_09,
        StatusCodes.STATUS_INPUT_ON_10,
        StatusCodes.STATUS_INPUT_ON_11,
        StatusCodes.STATUS_INPUT_ON_12,
        StatusCodes.STATUS_INPUT_ON_13,
        StatusCodes.STATUS_INPUT_ON_14,
        StatusCodes.STATUS_INPUT_ON_15
    };

    /* GTS status codes for Input-Off events */
    private static final int InputStatusCodes_OFF[] = new int[] {
        StatusCodes.STATUS_INPUT_OFF_00,
        StatusCodes.STATUS_INPUT_OFF_01,
        StatusCodes.STATUS_INPUT_OFF_02,
        StatusCodes.STATUS_INPUT_OFF_03,
        StatusCodes.STATUS_INPUT_OFF_04,
        StatusCodes.STATUS_INPUT_OFF_05,
        StatusCodes.STATUS_INPUT_OFF_06,
        StatusCodes.STATUS_INPUT_OFF_07,
        StatusCodes.STATUS_INPUT_OFF_08,
        StatusCodes.STATUS_INPUT_OFF_09,
        StatusCodes.STATUS_INPUT_OFF_10,
        StatusCodes.STATUS_INPUT_OFF_11,
        StatusCodes.STATUS_INPUT_OFF_12,
        StatusCodes.STATUS_INPUT_OFF_13,
        StatusCodes.STATUS_INPUT_OFF_14,
        StatusCodes.STATUS_INPUT_OFF_15
    };
    
    /* GTS status codes for Output-On events */
    private static final int OutputStatusCodes_ON[] = new int[] {
        StatusCodes.STATUS_OUTPUT_ON_00,
        StatusCodes.STATUS_OUTPUT_ON_01,
        StatusCodes.STATUS_OUTPUT_ON_02,
        StatusCodes.STATUS_OUTPUT_ON_03,
        StatusCodes.STATUS_OUTPUT_ON_04,
        StatusCodes.STATUS_OUTPUT_ON_05,
        StatusCodes.STATUS_OUTPUT_ON_06,
        StatusCodes.STATUS_OUTPUT_ON_07
    };

    /* GTS status codes for Output-Off events */
    private static final int OutputStatusCodes_OFF[] = new int[] {
        StatusCodes.STATUS_OUTPUT_OFF_00,
        StatusCodes.STATUS_OUTPUT_OFF_01,
        StatusCodes.STATUS_OUTPUT_OFF_02,
        StatusCodes.STATUS_OUTPUT_OFF_03,
        StatusCodes.STATUS_OUTPUT_OFF_04,
        StatusCodes.STATUS_OUTPUT_OFF_05,
        StatusCodes.STATUS_OUTPUT_OFF_06,
        StatusCodes.STATUS_OUTPUT_OFF_07
    };
    
    private int length;
    private int[] inputStates = new int[16];
    private int[] outputStates = new int[8];
    private int userAnalogADValue;
    private int rpm;
    private int fuel;
    private int engineTemperature;
    private int userDefinedADInput;
    private int futureUse1;
    private int futureUse2;
    private int futureUse3;
    private int futureUse4;
    
	public int getLength() {
		return length + 1;
	}
	
	public int[] getInputStates() {
		return inputStates;
	}
	
	public int[] getOutputStates() {
		return outputStates;
	}
	
	public int getUserAnalogADValue() {
		return userAnalogADValue;
	}
	
	public int getRpm() {
		return rpm;
	}
	
	public int getFuel() {
		return fuel;
	}
	
	public int getEngineTemperature() {
		return engineTemperature;
	}
	
	public int getUserDefinedADInput() {
		return userDefinedADInput;
	}
	
	public int getFutureUse1() {
		return futureUse1;
	}
	
	public int getFutureUse2() {
		return futureUse2;
	}
	
	public int getFutureUse3() {
		return futureUse3;
	}
	
	public int getFutureUse4() {
		return futureUse4;
	}
    
	public boolean readLength(Payload p)
	{
		length = p.readInt(1, 0);
		
		if(length != 0 && length != 16) 
		{
			Print.logError("Invalid length: " + length);
			return false;
		}
		
		return true;
		
	}
	
	public void readContent(Payload p) 
	{
		byte[] tempBytes;
		
		if(length == 0) {
			return;
		}
		
		if(length == 16) 
		{
			tempBytes = p.readBytes(2);
			
			for (int b = 0; b <= 7; b++) {
                long m = 1L << b;
                inputStates[b] = ((tempBytes[0] & m) != 0) ? InputStatusCodes_ON[b] : InputStatusCodes_OFF[b];
            }
			
			for (int b = 0; b <= 7; b++) {
                long m = 1L << b;
                inputStates[b + 8] = ((tempBytes[1] & m) != 0) ? InputStatusCodes_ON[b + 8] : InputStatusCodes_OFF[b + 8];
            }
			
			tempBytes = p.readBytes(1);
			
			for (int b = 0; b <= 7; b++) {
                long m = 1L << b;
                outputStates[b] = ((tempBytes[0] & m) != 0) ? OutputStatusCodes_ON[b] : OutputStatusCodes_OFF[b];
            }
			
			userAnalogADValue = p.readInt(2, 0);
			
			rpm = p.readInt(2, 0);
			
			fuel = p.readInt(1, 0);
			
			engineTemperature = p.readInt(2, 0);
			
			userDefinedADInput = p.readInt(2, 0);
			
			futureUse1 = p.readInt(1, 0);
			
			futureUse2 = p.readInt(1, 0);
			
			futureUse3 = p.readInt(1, 0);
			
			futureUse4 = p.readInt(1, 0);
			
		}
		else
		{
			Print.logError("Invalid length: " + length);
		}
	
	}
	
}
