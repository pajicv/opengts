package org.opengts.servers.geneko;

import org.opengts.util.Payload;
import org.opengts.util.Print;

public class OldBinaryHeader extends Header {
	
	public static int LENGTH = 8;
	
	public static byte[] START_OF_MESSAGE = {0x5C, 0x72};
	
    /* Message ID of GPS data message (Only IDs 0 is supported for now) */
    public static int MESSAGE_ID_REGULAR_GPS_DATA = 0; 
    
    public static		byte	MESSAGE_TYPE_NOT_ENCRYPTED_GPS = 0x56;
    
    public static		byte	MESSAGE_TYPE_NOT_ENCRYPTED_PARAMETER = 0x5A;
    
    
    
    public OldBinaryHeader() {
		super();
		length = LENGTH;
	}

	public boolean read(Payload p)
    {
    	
    	/* Extract message type */
        messageLength = p.readInt(1, 0);
    	
    	/* Extract message type */
        messageType = p.readInt(1, 0);
		
		Print.logInfo("Msg. type: " + messageType);
        
        if(messageType != MESSAGE_TYPE_NOT_ENCRYPTED_GPS) {
        	Print.logWarn("WARNING: Only Fox/FoxLite not encrypted GPS data messages are supported");
        	return false;
        }
        
        /* Extract the vehicle ID*/
        long  vid = p.readULong(4, 0L, true);
        vehicleId = String.valueOf(vid);
        
        Print.logInfo("Vehicle ID: " + vehicleId);
        
        /* Extract message id */
        messageId = p.readUInt(2, 0);
        if(messageId != MESSAGE_ID_REGULAR_GPS_DATA)
        {
        	Print.logWarn("WARNING: Only regular GPS data messages (id = 0) are supported");
        	return false;
        }
        
        return true;
    }

}
