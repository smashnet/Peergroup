/*
* Peergroup - Request.java
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
public class Request {
	
	/**
	* The type specifies what kind of request this is
	*/
	private int type;
	
	/**
	* The lamport time for this request (to reconstruct an ordering of requests)
	*/
	private long lamportTime;
	
	/**
	* The content of this request
	*/
	private String content;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public Request(int newID, String newContent){
		this.type = newID;
		this.lamportTime = -1;
		this.content = newContent;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public Request(int newID, long newLamp, String newContent){
		this.type = newID;
		this.lamportTime = newLamp;
		this.content = newContent;
	}
	
	public void setID(int newID){
		this.type = newID;
	}
	
	public void setLamport(long newLamp){
		this.lamportTime = newLamp;
	}
	
	public void setContent(String newContent){
		this.content = newContent;
	}
	
	public int getID(){
		return this.type;
	}
	
	public long getLamportTime(){
		return this.lamportTime;
	}
	
	public String getContent(){
		return this.content;
	}
}
