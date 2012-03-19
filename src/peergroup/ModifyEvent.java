/*
* Peergroup - ModifyEvent.java
* 
* Peergroup is a file synching tool using XMPP for data- and 
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
	
	private String filename;
	private long time;
	
	/**
	* Creates a ModifyEvent.
	*/
	public ModifyEvent(String name){
		this.filename = name;
		this.time = System.currentTimeMillis();
	}
	
	public void setName(String newName){
		this.filename = newName;
	}
	
	public void setTime(long timestamp){
		this.time = timestamp;
	}
	
	public String getName(){
		return this.filename;
	}
    
	public long getTime(){
		return this.time;
	}
}
