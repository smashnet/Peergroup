/*
* Peergroup - XMPPRequest.java
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

import org.jivesoftware.smack.packet.*;

/**
 * A general request consisting of a type and the content
 *
 * @author Nicolas Inden
 */
public class XMPPRequest extends Request{
	
	/**
	* The content of this request
	*/
	private Message content;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public XMPPRequest(int newID, Message newMessage){
		super(newID);
		this.content = newMessage;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public XMPPRequest(int newID, long newLamp, Message newMessage){
		super(newID,newLamp);
		this.content = newMessage;
	}
	
	public void setID(int newID){
		this.type = newID;
	}
	
	public void setLamport(long newLamp){
		this.lamportTime = newLamp;
	}
	
	public void setContent(Message newContent){
		this.content = newContent;
	}
	
	public int getID(){
		return this.type;
	}
	
	public long getLamportTime(){
		return this.lamportTime;
	}
	
	public Message getContent(){
		return this.content;
	}
}
