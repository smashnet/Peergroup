/*
* Peergroup - FSRequest.java
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
 * A general request consisting of a type and the content
 *
 * @author Nicolas Inden
 */
public class FSRequest extends Request{
	
	/**
	* The content of this request
	*/
	private String content;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public FSRequest(int newID, String newContent){
		super(newID);
		this.content = newContent;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public FSRequest(int newID, long newLamp, String newContent){
		super(newID,newLamp);
		this.content = newContent;
	}
	
	public void setID(int newID){
		super.type = newID;
	}
	
	public void setLamport(long newLamp){
		super.lamportTime = newLamp;
	}
	
	public void setContent(String newContent){
		this.content = newContent;
	}
	
	public int getID(){
		return super.type;
	}
	
	public long getLamportTime(){
		return super.lamportTime;
	}
	
	public String getContent(){
		return this.content;
	}
}
