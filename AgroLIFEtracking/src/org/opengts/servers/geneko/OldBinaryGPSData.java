package org.opengts.servers.geneko;

import java.util.Calendar;
import java.util.TimeZone;

import org.opengts.util.DateTime;
import org.opengts.util.GeoPoint;
import org.opengts.util.Payload;
import org.opengts.util.Print;

public class OldBinaryGPSData extends GPSData {
	
	private static final int LENGTH = 18;
	
	private static final int GPS_VALID = 0x00;
	
	private static final int GPS_NOT_VALID = 0x80;
	
	public static final double  KILOMETERS_PER_KNOT = 1.85200000;
	
	protected int gpsValid;
	
	public void read(Payload p) 
	{
	
		Print.logInfo("Hex. GPS data: " + p.toString());
	
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		byte[] tempBytes;
		
		/* Extract year and GPS valid flag */
        tempBytes = p.readBytes(1, "");
        year = (int) (tempBytes[0] & 0x7F);
        gpsValid = (int) ((tempBytes[0] & 0x80) >> 7); 
		
		Print.logInfo("Hex: " + Integer.toHexString(year) + " Year: " + year);
		Print.logInfo("Hex: " + Integer.toHexString(gpsValid) + " Valid: " + gpsValid);
        
        /* Extract month, day, hour, minute, second */
        month = p.readUInt(1, 0);
		Print.logInfo("Hex: " + Integer.toHexString(month) + " Month: " + month);
		
        day = p.readUInt(1, 0);
		Print.logInfo("Hex: " + Integer.toHexString(day) + " Day: " + day);
		
        hour = p.readUInt(1, 0);
		Print.logInfo("Hex: " + Integer.toHexString(hour) + " Hour: " + hour);
		
        minute = p.readUInt(1, 0);
		Print.logInfo("Hex: " + Integer.toHexString(minute) + " Minute: " + minute);
		
        second = p.readUInt(1, 0);
		Print.logInfo("Hex: " + Integer.toHexString(second) + " Second: " + second);
        
        /* Generate timestamp */
        year += 2000;
        DateTime dt = new DateTime(utcTimezone, year, month, day, hour, minute, second);
        long timeInMillis = dt.getTimeMillis();
        long offsetToUTC = TimeZone.getTimeZone("Europe/Belgrade").getOffset(timeInMillis);
        dt.setTimeMillis(timeInMillis + offsetToUTC);
        timestamp = dt.getTimeSec();
		
        
        /* Extract latitude */
        
		
        tempBytes = p.readBytes(4, "");
        int h = (int) ((tempBytes[0] & 0x80) >> 7);
        char hemisphere = 'N';
        if(h == 1) {
        	hemisphere = 'S';
        }
        int latMinutesMSB = (int) (tempBytes[0] & 0x7F);
        int latMinutesLSB = (int) (tempBytes[1] & 0xFF);
        int latMinutesMSBDecimalPart = (int) (tempBytes[2] & 0xFF);
        int latMinutesLSBDecimalPart = (int) (tempBytes[3] & 0xFF);
        latitude = calculateCoordinate(hemisphere, latMinutesMSB, latMinutesLSB, latMinutesMSBDecimalPart, latMinutesLSBDecimalPart);
		
		Print.logInfo("Latitude");
		Print.logInfo("Hex: " + Integer.toHexString(h) + " Hemisphere: " + hemisphere);
        Print.logInfo("Hex: " + Integer.toHexString(latMinutesMSB) + " Lat. minutes. MSB: " + latMinutesMSB);
		Print.logInfo("Hex: " + Integer.toHexString(latMinutesLSB) + " Lat. minutes. LSB: " + latMinutesLSB);
		Print.logInfo("Hex: " + Integer.toHexString(latMinutesMSBDecimalPart) + " Lat. minutes. dec. MSB: " + latMinutesMSBDecimalPart);
		Print.logInfo("Hex: " + Integer.toHexString(latMinutesLSBDecimalPart) + " Lat. minutes. dec. LSB: " + latMinutesLSBDecimalPart);
		Print.logInfo("Latitude min.: " + (latMinutesMSB * 256 + latMinutesLSB) + "." + (latMinutesMSBDecimalPart * 256 + latMinutesLSBDecimalPart));
		Print.logInfo("Latitude: " + latitude);
		
        /* Extract longitude */
        
        tempBytes = p.readBytes(4, "");
        h = (int) ((tempBytes[0] & 0x80) >> 7);
        hemisphere = 'E';
        if(h == 1) {
        	hemisphere = 'W';
        }
        int lonMinutesMSB = (int) (tempBytes[0] & 0x7F);
        int lonMinutesLSB = (int) (tempBytes[1] & 0xFF);
        int lonMinutesMSBDecimalPart = (int) (tempBytes[2] & 0xFF);
        int lonMinutesLSBDecimalPart = (int) (tempBytes[3] & 0xFF);
        
        longitude = calculateCoordinate(hemisphere, lonMinutesMSB, lonMinutesLSB, lonMinutesMSBDecimalPart, lonMinutesLSBDecimalPart);
        
		Print.logInfo("Longitude");
		Print.logInfo("Hex: " + Integer.toHexString(h) + " Hemisphere: " + hemisphere);
        Print.logInfo("Hex: " + Integer.toHexString(lonMinutesMSB) + " Lon. minutes. MSB: " + lonMinutesMSB);
		Print.logInfo("Hex: " + Integer.toHexString(lonMinutesLSB) + " Lon. minutes. LSB: " + lonMinutesLSB);
		Print.logInfo("Hex: " + Integer.toHexString(lonMinutesMSBDecimalPart) + " Lon. minutes. dec. MSB: " + lonMinutesMSBDecimalPart);
		Print.logInfo("Hex: " + Integer.toHexString(lonMinutesLSBDecimalPart) + " Lon. minutes. dec. LSB: " + lonMinutesLSBDecimalPart);
		Print.logInfo("Longitude min.: " + (lonMinutesMSB * 256 + lonMinutesLSB) + "." + (lonMinutesMSBDecimalPart * 256 + lonMinutesLSBDecimalPart));
		Print.logInfo("Longitude: " + longitude);
		
        /* Extract speed and heading */
        int s = p.readInt(2, 0);
        speed = s * KILOMETERS_PER_KNOT;
        heading = (double) p.readInt(2, 0);
	}
	
	public boolean isValid() {
		return gpsValid == GPS_VALID;
	}	
	
	public int getLength() {
		return LENGTH;
	}

}
