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
    	double coordinate = (((double)(wholePartMSB * 256 + wholePartLSB) + ((double)(decimalPartMSB * 256 + decimalPartLSB) / 10000.0D))) / 60.0D;
		if((hemisphere == 'S') || (hemisphere == 'W')) {
			coordinate *= -1.0D;
		}
    	return coordinate;
    }
	
}
