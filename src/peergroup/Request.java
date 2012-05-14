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
	protected int type;
	
	/**
	* The lamport time for this request (to reconstruct an ordering of requests)
	*/
	protected long lamportTime;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public Request(int newID){
		this.type = newID;
		this.lamportTime = -1;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public Request(int newID, long newLamp){
		this.type = newID;
		this.lamportTime = newLamp;
	}
	
	public void setID(int newID){
		this.type = newID;
	}
	
	public void setLamport(long newLamp){
		this.lamportTime = newLamp;
	}
	
	public int getID(){
		return this.type;
	}
	
	public long getLamportTime(){
		return this.lamportTime;
	}
}
