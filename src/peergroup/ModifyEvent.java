/*
* Peergroup - ModifyEvent.java
* 
* Peergroup is a P2P Shared Storage System using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

package peergroup;

/**
 * The ModifyEvent contains a filename, and a timestamp for the
 * last time a modify event arrived.
 *
 * @author Nicolas Inden
 */
public class ModifyEvent{
	
	private int type;
	private String filename;
	private long time;
	
	public ModifyEvent(int newType, String name){
		this.type = newType;
		this.filename = name;
		this.time = System.currentTimeMillis();
	}
	
	public ModifyEvent(String name){
		this.type = Constants.LOCAL_ENTRY_MODIFY;
		this.filename = name;
		this.time = System.currentTimeMillis();
	}
	
	public void setName(String newName){
		this.filename = newName;
	}
	
	public void setTime(long timestamp){
		this.time = timestamp;
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getName(){
		return this.filename;
	}
    
	public long getTime(){
		return this.time;
	}
}
