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
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import de.pgrp.thrift.*;

import java.nio.ByteBuffer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

/**
 * This class lets your access information about a participant in your network.
 * 
 * @author Nicolas Inden
 */
public class P2Pdevice {

	private String remoteIP;
	private String localIP;
	private int port;
	private String jid;
	private TTransport transport;
	private DataTransfer.Client client;
	private boolean usingLocalIP;

	public P2Pdevice() {

	}

	public P2Pdevice(String newJID, String newRemoteIP, String newLocalIP, int newPort) {
		this.jid = newJID;
		this.remoteIP = newRemoteIP;
		this.localIP = newLocalIP;
		this.port = newPort;
		this.transport = new TSocket(newRemoteIP, newPort);
		this.usingLocalIP = false;
	}

	private void openTransport() {
		try {
			TProtocol protocol = new TBinaryProtocol(this.transport);
			this.client = new DataTransfer.Client(protocol);
			transport.open();
		} catch (TTransportException e) {
			Globals.log.addMsg("Thrift Error: " + e);
		}
	}

	public void closeTransport() {
		// if(this.transport.isOpen())
		this.transport.close();
	}

	public synchronized byte[] getDataBlock(String name, int id, String hash) {
		if (!this.transport.isOpen()) {
			openTransport();
		}
		try {
			ByteBuffer block = client.getDataBlock(name, id, hash);

			this.transport.close();
			return block.array();
		} catch (TException te) {
			Globals.log
					.addMsg("Error downloading chunk " + id + "! " + te, 1);
			Globals.log.addMsg("Attempting to redownload.");
			this.transport.close();
			return null;
		}
	}

	public synchronized ThriftStorage getFileList() {
		if (!this.transport.isOpen()) {
			openTransport();
		}
		try {
			ThriftStorage list = client.getStorage();

			this.transport.close();
			return list;
		} catch (TException te) {
			Globals.log.addMsg("Thrift Error: " + te, 1);
		}
		return null;
	}

	public boolean transportOpen() {
		return this.transport.isOpen();
	}

	
	
	/**
	 * If the external IP address of this P2Pdevice equals mine, then check if we are in the same
	 * local net by comparing hash(conference_pass + first local IP block + second local IP block).
	 */
	private void checkLocal(){
		if(this.remoteIP.equals(Globals.externalIP4)){
			String myLocalIP[] = Globals.internalIP4.split("\\.");
			String devicesLocalIP[] = this.localIP.split("\\.");
			int matches = 0;
			
			for(int i = 0; i < myLocalIP.length; i++){
				if(myLocalIP[i].equals(devicesLocalIP[i])){
					matches++;
				}else{
					break;
				}
			}
			//If at least A and B of local addresses are equal, we assume we are on the same LAN
			if(matches >= 2){
				this.usingLocalIP = true;
				this.transport = new TSocket(this.localIP, this.port);
				Globals.log.addMsg("Using local IP for " + this.jid + ": " + this.localIP,2);
			}
			/* Verify with thrift (only works of remotely accessible)
			openTransport();
			try {
				String localIP[] = Globals.localIP.split("\\.");
				String toBeHashed = Globals.conference_pass + localIP[0] + localIP[1];
				MessageDigest hash = MessageDigest.getInstance(Globals.hashAlgo);
				hash.update(toBeHashed.getBytes("UTF-8"));
			
				String peerLocalIP = client.getLocalIP(hash.toString());
				
				this.closeTransport();
				
				if(peerLocalIP != null){
					Globals.log.addMsg("Using local IP for " + this.jid + ": " + peerLocalIP,2);
					this.localIP = peerLocalIP;
					this.transport = new TSocket(this.localIP, this.port);
					this.usingLocalIP = true;
				}
			} catch (TException te) {
				Globals.log.addMsg("Thrift error: " + te, 1);
			} catch (NoSuchAlgorithmException na) {
				Globals.log.addMsg("Hash error: " + na, 1);
			} catch (UnsupportedEncodingException uee) {
				Globals.log.addMsg("Encoding error: " + uee, 1);
			}*/
		}
	}

	public boolean equals(String newJID, String newRemoteIP, String newLocalIP, int newPort) {
		if (this.port != newPort) {
			return false;
		}
		if (!this.remoteIP.equals(newRemoteIP)) {
			return false;
		}
		if (!this.localIP.equals(newLocalIP)) {
			return false;
		}
		if (!this.jid.equals(newJID)) {
			return false;
		}
		return true;
	}
	
	public String getJID() {
		return this.jid;
	}

	public String getRemoteIP() {
		return this.remoteIP;
	}
	
	public String getLocalIP() {
		return this.localIP;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isLocal() {
		return this.usingLocalIP;
	}
	
	public String getUsedIP() {
		if(this.isLocal())
			return this.localIP;
		
		return this.remoteIP;
	}
	
	/**
	 * Looks for an equal existing P2Pdevice in the global list and returns it.
	 * If not existent, the supplied P2Pdevice is returned.
	 */
	public static P2Pdevice getDevice(String newJID, String newRemoteIP, String newLocalIP, int newPort) {
		for (P2Pdevice d : Globals.p2pDevices) {
			if (d.equals(newJID, newRemoteIP, newLocalIP, newPort))
				return d;
		}
		P2Pdevice newPeer = new P2Pdevice(newJID, newRemoteIP, newLocalIP, newPort);
		Globals.p2pDevices.add(newPeer);
		newPeer.checkLocal();
		return newPeer;
	}
}
