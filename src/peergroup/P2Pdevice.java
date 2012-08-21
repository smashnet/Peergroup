/*
* Peergroup - P2Pdevice.java
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

import java.nio.ByteBuffer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;


/**
 * This class lets your access information about a participant in your
 * network.
 *
 * @author Nicolas Inden
 */
public class P2Pdevice {
    
	private String ip;
	private int port;
	private String jid;
	private TTransport transport;
	private DataTransfer.Client client;

	public P2Pdevice(){
		
	}

	/**
	* Use this constructor to add a new P2Pdevice
	*/
	public P2Pdevice(String newJID,String newIP, int newPort){
		this.jid = newJID;
		this.ip = newIP;
		this.port = newPort;
		this.transport = new TSocket(newIP, newPort);
	}
	
	private void openTransport(){
		try{
			TProtocol protocol = new TBinaryProtocol(this.transport);
			this.client = new DataTransfer.Client(protocol);
			transport.open();
		}catch(TTransportException e){
			Constants.log.addMsg("Thrift Error: " + e);
		}catch(TException e){
			Constants.log.addMsg("Thrift Error: " + e);
		}
	}
	
	public void closeTransport(){
		//if(this.transport.isOpen())
			this.transport.close();
	}
	
	public synchronized byte[] getDataBlock(String name, int id, String hash){
		if(!this.transport.isOpen()){
			openTransport();
		}
		try{
			ByteBuffer block = client.getDataBlock(name,id,hash);
		
			return block.array();
		}catch(TException te){
			Constants.log.addMsg("Error downloading chunk " + id + "! " + te,1);
			Constants.log.addMsg("Attempting to redownload.");
			this.transport.close();
			return null;
		}
	}
	
	public synchronized ThriftStorage getFileList(){
		if(!this.transport.isOpen()){
			openTransport();
		}
		try{
			ThriftStorage list = client.getStorage();
		
			return list;
		}catch(TException te){
			Constants.log.addMsg("Thrift Error: " + te,1);
		}
		return null;
	}
	
	public boolean transportOpen(){
		return this.transport.isOpen();
	}
	
	/**
	* Looks for an equal existing P2Pdevice in the global list and returns it.
	* If not existent, the supplied P2Pdevice is returned.
	*/
	public static P2Pdevice getDevice(String newJID,String newIP, int newPort){
		for(P2Pdevice d : Constants.p2pDevices){
			if(d.equals(newJID,newIP,newPort))
				return d;
		}
		P2Pdevice newPeer = new P2Pdevice(newJID,newIP,newPort);
		Constants.p2pDevices.add(newPeer);
		return newPeer;
	}
	
	public boolean equals(P2Pdevice node){
		if(this.port != node.getPort()){
			return false;
		}
		if(!this.ip.equals(node.getIP())){
			return false;
		}
		if(!this.jid.equals(node.getJID())){
			return false;
		}
		return true;
	}
	
	public boolean equals(String newJID,String newIP, int newPort){
		if(this.port != newPort){
			return false;
		}
		if(!this.ip.equals(newIP)){
			return false;
		}
		if(!this.jid.equals(newJID)){
			return false;
		}
		return true;
	}
	
	public String getIP(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getJID(){
		return this.jid;
	}
}
