/*
* Peergroup - DLRequest.java
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

/**
 * A general request consisting of a type and the content
 *
 * @author Nicolas Inden
 */
public class DLRequest extends Request{
	
	private String name;
	private int blockID;
	private int version;
	private String hash;
	private P2Pdevice node;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public DLRequest(int newID, int vers, String newName, int newBlockID, String newHash, P2Pdevice newNode){
		super(newID);
		this.name = newName;
		this.blockID = newBlockID;
		this.version = vers;
		this.hash = newHash;
		this.node = newNode;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public DLRequest(int newID, int vers, String newName, int newBlockID, String newHash, P2Pdevice newNode, long newLamp){
		super(newID,newLamp);
		this.name = newName;
		this.blockID = newBlockID;
		this.version = vers;
		this.hash = newHash;
		this.node = newNode;
	}
	
	public void setID(int newID){
		super.type = newID;
	}
	
	public void setLamport(long newLamp){
		super.lamportTime = newLamp;
	}
	
	public int getID(){
		return super.type;
	}
	
	public long getLamportTime(){
		return super.lamportTime;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getBlockID(){
		return this.blockID;
	}
	
	public String getHash(){
		return this.hash;
	}
	
	public P2Pdevice getNode(){
		return this.node;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setVersion(int vers){
		this.version = vers;
	}
}
