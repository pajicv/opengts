// ----------------------------------------------------------------------------
// Copyright 2007-2014, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
// Astra Telematics device communication server with support for:
//   Protocol C
//   Protocol K
//   Protocol M
// ----------------------------------------------------------------------------
// Change History:
//  2012/11/27  Richard R. Patel
//     -Initial release
//  2014/02/04  Richard R. Patel
//     -Added support for Protocol C
//     -Added check for more packet data whilst parsing reports
// ----------------------------------------------------------------------------
package org.opengts.servers.geneko;

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;
import org.opengts.servers.*;

public class TrackClientPacketHandler
    extends AbstractClientPacketHandler
{

    // ------------------------------------------------------------------------
	public static       String  UNIQUEID_PREFIX[]           = new String[0];
    public static       double  MINIMUM_SPEED_KPH           = 5.0;
    public static       boolean ESTIMATE_ODOMETER           = false;
    public static       boolean SIMEVENT_GEOZONES           = false;
    public static       boolean XLATE_LOCATON_INMOTION      = false;
    public static       double  MINIMUM_MOVED_METERS        = 0.0;   

    public static       boolean DEBUG_MODE                  = false;

    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------

    /* session IP address */
    private String          ipAddress                   = null;
    private int             clientPort                  = 0;

    /* current device */
    private Device          device                      = null;

    /* Extra report data added to rawData field if true */
    private boolean         addRawData                  = true;
    

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* packet handler constructor */
    public TrackClientPacketHandler() 
    {
        super();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* callback when session is starting */
    public void sessionStarted(InetAddress inetAddr, boolean isTCP, boolean isText)
    {
        super.sessionStarted(inetAddr, isTCP, isText);
        super.clearTerminateSession();

        /* init */
        this.ipAddress        = (inetAddr != null)? inetAddr.getHostAddress() : null;
        this.clientPort       = this.getSessionInfo().getRemotePort();
    }

    /* callback when session is terminating */
    public void sessionTerminated(Throwable err, long readCount, long writeCount)
    {
        super.sessionTerminated(err, readCount, writeCount);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* based on the supplied packet data, return the remaining bytes to read in the packet */
    public int getActualPacketLength(byte packet[], int packetLen)
    {
        int packetLength = 0;
		
		Print.logInfo("Received length" + packetLen);
        
        /* Determine message type */
        Payload p = new Payload(packet, 0, 2);
        
        int msgType;
        
        int b1 = p.readUInt(1, 0);
        int b2 = p.readUInt(1, 0);

        /* Determine the full packet length */
        if(b1 == 0x5C && b2 == 0x72) {
        	p = new Payload(packet, 2, 1);
            packetLength = p.readUInt(1, 0);
        } else if(b1 == 0x5C && b2 == 0x65) {
        	p = new Payload(packet, 2, 3);
            packetLength = p.readUInt(3, 0);
        } else {
        	if (DEBUG_MODE)
            {
                Print.logInfo("Invalid message type");
            }
        }
        
		Print.logInfo("Message length" + packetLength);
        
        /* (debug message) log received packet length */
        if (DEBUG_MODE)
        {
            Print.logInfo("Packet length: " + packetLength);
        }

        /* Let 'getHandlePacket' method process the complete packet */
        return packetLength;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* workhorse of the packet handler */
    public byte[] getHandlePacket(byte pktBytes[]) 
    {
        if ((pktBytes != null) && (pktBytes.length > 0))
        {
        	
	            /* (debug message) display received data packet */
	            if (DEBUG_MODE)
	            {
	                Print.logInfo("Recv[HEX]: " + StringTools.toHexString(pktBytes));
	            }
	
	            /* Parse packet contents and insert data into database */
				
				Print.logInfo("Recv[HEX]: " + StringTools.toHexString(pktBytes));
	            
                parseInsertRecord(pktBytes);
                
                return null;
                
        }
        else
        {
            /* no packet received */
            Print.logInfo("Empty packet received ...");
            return null; // no return packets are expected
        }
    }


    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean parseInsertRecord(byte pktBytes[])
    {
        int i;
        int index;
        int checksum;
        int packetChecksum;
        int statusCode;
        String rawData = "";
        Header header;
        GPSData gpsData;
        CellName cellName = new CellName();
        VehicleStatus vehicleStatus = new VehicleStatus();
        GenekoEventData0 eventData0 = new GenekoEventData0();

        /* Generate 1 byte packet checksum for all bytes except the last 1 */
        Payload p = new Payload(pktBytes, (pktBytes.length - 1), 1);
        packetChecksum = p.readUInt(1, 0);
        
        checksum = generateCheckSum (pktBytes, pktBytes.length);
		
		
		Print.logInfo("Checksum: " + checksum);
		
		
        if (checksum != packetChecksum)
        {
            Print.logError("ERROR: Calculated checksum does not match packet checksum");
            return false;
        }
        
        /* Check format */
        
        p = new Payload(pktBytes, 0, 2);
        byte[] messageStart = p.readBytes(2);
        
        if(messageStart[0] == OldBinaryHeader.START_OF_MESSAGE[0] || messageStart[1] == OldBinaryHeader.START_OF_MESSAGE[1])
    	{
        	header = new OldBinaryHeader();
        	gpsData = new OldBinaryGPSData();
    	} else { 
    		return false;
    	}

        index = 2;
        
        p = new Payload(pktBytes, index, header.getLength());
        if(!header.read(p)) 
        {
        	/* Error already logged */
            return false;
        }
        
        /* Find the device in the database */
        device = DCServerFactory.loadDeviceByAccountDeviceID("demo", header.getVehicleId());
        
        if (device == null)
        {
            /* Error already logged */
            return false;
        }
            
        index += header.getLength();
        
        p = new Payload(pktBytes, index, gpsData.getLength());
        gpsData.read(p);
        
        index += gpsData.getLength();
        
        p = new Payload(pktBytes, index, 1);
        if(!cellName.readLength(p))
        {
        	/* Error already logged */
            return false;
        }
        p = new Payload(pktBytes, index, cellName.getLength());
        cellName.readContent(p);
        
        index += cellName.getLength();
        
        p = new Payload(pktBytes, index, 1);
        if(!vehicleStatus.readLength(p))
        {
        	/* Error already logged */
            return false;
        }
        p = new Payload(pktBytes, index, vehicleStatus.getLength());
        vehicleStatus.readContent(p);
        
        index += vehicleStatus.getLength();
        
        p = new Payload(pktBytes, index, 1);
        if(!eventData0.readLength(p))
        {
        	/* Error already logged */
            return false;
        }
        p = new Payload(pktBytes, index, eventData0.getLength());
        eventData0.readContent(p);
            
        /* (debug message) log report data */
        if (DEBUG_MODE)
        {
            Print.logInfo("Latitude: " + gpsData.getLatitude());
            Print.logInfo("Longitude: " + gpsData.getLongitude());
            Print.logInfo("Time: " + gpsData.getTimestamp());
            Print.logInfo("Speed: " + gpsData.getSpeed());
            Print.logInfo("Heading: " + gpsData.getHeading());
            if(eventData0.getLength() > 0) 
            {
            	Print.logInfo("Journey Dist: " + eventData0.getTotalDistanceTraveled());
            }
            if(vehicleStatus.getLength() > 0) 
            {
            	Print.logInfo("RPM: " + vehicleStatus.getRpm());
            	Print.logInfo("Fuel: " + vehicleStatus.getFuel());
            }
        }

        /* One status code per report so determine which one is most relevant */
        statusCode = StatusCodes.STATUS_LOCATION;

        /* Extra report data to be added to rawData field ? */
        if (addRawData)
        {
            rawData = StringTools.toHexString(pktBytes);
        }

        /* Insert record into EventData table */
        insertEventRecord(device, gpsData, vehicleStatus, eventData0, statusCode, rawData);

        return true;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private int generateCheckSum (byte[] bytPacketDatam, int packetLen)
    {
        int sum = 0;
        int checksum;
        int byteVal;
        int i; 		  

        for (i = 2; i < (packetLen) - 1; i++)
        {
            byteVal = (int) bytPacketDatam[i] & 0xFF;
            sum += byteVal;
        }
        
        checksum = 256 - (sum % 256);

        return (checksum);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private EventData createEventRecord(Device device, 
        long     gpsTime, 
        int statusCode, 
        double latitude, 
        double longitude, 
        double speedKPH, 
        double heading, 
        double distanceKM, 
        int rpm, 
        int fuel, 
        String rawData)
    {
        String accountID    = device.getAccountID();
        String deviceID     = device.getDeviceID();
        EventData.Key evKey = new EventData.Key(accountID, deviceID, gpsTime, statusCode);
        EventData evdb      = evKey.getDBRecord();
        evdb.setLatitude(latitude);
        evdb.setLongitude(longitude);
        evdb.setSpeedKPH(speedKPH);
        evdb.setHeading(heading);
        evdb.setDistanceKM(distanceKM);
        evdb.setEngineRpm(rpm);
        evdb.setFuelLevel(fuel);
        evdb.setRawData(rawData);
        return evdb;
    }

    /* create and insert an event record */
    private void insertEventRecord(Device device, 
        GPSData gpsd, 
        VehicleStatus vs, 
        GenekoEventData0 ed0,  
        int sc, 
        String rawData)
    {
        /* create event */
        EventData evdb = createEventRecord(device, 
            gpsd.getTimestamp(), 
            sc,
            gpsd.getLatitude(),
            gpsd.getLongitude(),
            gpsd.getSpeed(),
            gpsd.getHeading(),
            ed0.getTotalDistanceTraveled(),
            vs.getRpm(),
            vs.getFuel(), 
            rawData);

        /* insert event */
        // this will display an error if it was unable to store the event
        Print.logInfo("Event: [0x" + StringTools.toHexString(sc,16) + "] " + 
            StatusCodes.GetDescription(sc,null));
        if (!DEBUG_MODE) {
            device.insertEventData(evdb);
            this.incrementSavedEventCount();
        }
    }
    

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void configInit() 
    {
        DCServerConfig dcsc = Main.getServerConfig();
        if (dcsc != null) {
            UNIQUEID_PREFIX         = dcsc.getUniquePrefix();
            MINIMUM_SPEED_KPH       = dcsc.getMinimumSpeedKPH(MINIMUM_SPEED_KPH);
            ESTIMATE_ODOMETER       = dcsc.getEstimateOdometer(ESTIMATE_ODOMETER);
            SIMEVENT_GEOZONES       = dcsc.getSimulateGeozones(SIMEVENT_GEOZONES);
            XLATE_LOCATON_INMOTION  = dcsc.getStatusLocationInMotion(XLATE_LOCATON_INMOTION);
            MINIMUM_MOVED_METERS    = dcsc.getMinimumMovedMeters(MINIMUM_MOVED_METERS);
        }
    }

    // ------------------------------------------------------------------------

}
