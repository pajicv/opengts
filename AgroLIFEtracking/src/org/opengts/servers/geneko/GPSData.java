package org.opengts.servers.geneko;

import java.util.TimeZone;

import org.opengts.util.DateTime;
import org.opengts.util.Payload;

public abstract class GPSData {

	protected static final TimeZone utcTimezone = DateTime.getTimeZone("UTC");
	
	protected int length;
	protected long timestamp;
	protected double latitude;
	protected double longitude;
	protected double speed;
	protected double heading;
	
	public abstract void read(Payload p);
	
	public abstract int getLength();

	public long getTimestamp() {
		return timestamp;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getSpeed() {
		return speed;
	}

	public double getHeading() {
		return heading;
	}
	
	protected double calculateCoordinate(char hemisphere, int wholePartMSB, 
    		int wholePartLSB, int decimalPartMSB, int decimalPartLSB) 
    {
    	double coordinate = (((wholePartMSB * 256 + wholePartLSB) + ((decimalPartMSB * 256 + decimalPartLSB) / 10000))) / 60;
		if((hemisphere == 'S') || (hemisphere == 'W')) {
			coordinate *= -1;
		}
    	return coordinate;
    }
	
}
