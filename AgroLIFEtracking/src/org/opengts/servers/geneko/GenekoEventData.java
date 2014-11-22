package org.opengts.servers.geneko;

import org.opengts.util.Payload;

public abstract class GenekoEventData {

	protected int length;
	
	public int getLength() {
		return length;
	}
	
	public abstract boolean readLength(Payload p);
	
	public abstract void readContent(Payload p);
	
}
