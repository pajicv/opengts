package org.opengts.servers.geneko;

import org.opengts.util.Payload;
import org.opengts.util.Print;

public abstract class Header {
	
	protected int length;
	protected int messageLength;
	protected int messageType;
	protected String vehicleId;
	protected int messageId;
	
	public abstract boolean read(Payload p);
	
	public int getLength() {
		return length;
	}

	public int getMessageLength() {
		return messageLength;
	}

	public int getMessageType() {
		return messageType;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public int getMessageId() {
		return messageId;
	}
	
	public void print() {
		Print.logDebug("Message Length: %s".format(Integer.toHexString(messageLength)));
		Print.logDebug("Message Type: %s".format(Integer.toHexString(messageType)));
		Print.logDebug("Vehicle Id: %s".format(vehicleId));
		Print.logDebug("Message Id: %s".format(Integer.toHexString(messageId)));
    }

}
