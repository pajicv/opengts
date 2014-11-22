package org.opengts.servers.geneko;

import org.opengts.util.Payload;
import org.opengts.util.Print;

public class GenekoEventData0 extends GenekoEventData {

	private long totalDistanceTraveled;
	private double maxSpeedBetween;
	
	@Override
	public boolean readLength(Payload p) {
		// TODO Auto-generated method stub
		
		length = p.readInt(1, 0);
		
		if(length != 4 && length != 6) 
		{
			Print.logError("Invalid length: " + length);
			return false;
		}
		
		return true;
	}

	public long getTotalDistanceTraveled() {
		return totalDistanceTraveled;
	}

	public double getMaxSpeedBetween() {
		return maxSpeedBetween;
	}

	@Override
	public void readContent(Payload p) {
		// TODO Auto-generated method stub
		
		totalDistanceTraveled = p.readLong(4, 0);
		if(length == 6) 
		{
			int integerPart = p.readInt(1, 0);
			int decimalPart = p.readInt(1, 0);
			maxSpeedBetween = integerPart + decimalPart / 100;
		}
	}

}
