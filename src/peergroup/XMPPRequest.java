/*
* Peergroup - XMPPRequest.java
* 
* This file is part of Peergroup.
*
* Peergroup is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Peergroup is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* Copyright (c) 2012 Nicolas Inden
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
