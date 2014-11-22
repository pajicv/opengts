package org.opengts.servers.geneko;

import org.opengts.util.Payload;
import org.opengts.util.Print;

public class CellName {
		
	private int length;
	
	private String name;
	
	public boolean readLength(Payload p)
	{
		length = p.readInt(1, 0);
		
		if(length < 0 || length > 25) 
		{
			Print.logError("Invalid length: " + length);
			return false;
		}
		
		return true;
	}
	
	public void readContent(Payload p) 
	{
		name = p.readString(length);		
	}
	
	public int getLength() {
		return length + 1;
	}

	public String getName() {
		return name;
	}

}
