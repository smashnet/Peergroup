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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

/**
 * This class lets your access information about a participant in your network.
 * 
 * @author Nicolas Inden
 */
public class P2Pdevice {

	private String ip;
	private String localIP;
	private int port;
	private String jid;
	private TTransport transport;
	private DataTransfer.Client client;
	private boolean usingLocalIP;

	public P2Pdevice() {

	}

	/**
	 * Use this constructor to add a P2Pdevice.getDevice
	 */
	public P2Pdevice(String newJID, String newIP, int newPort) {
		this.jid = newJID;
		this.ip = newIP;
		this.port = newPort;
		this.transport = new TSocket(newIP, newPort);
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
	 * Looks for an equal existing P2Pdevice in the global list and returns it.
	 * If not existent, the supplied P2Pdevice is returned.
	 */
	public static P2Pdevice getDevice(String newJID, String newIP, int newPort) {
		for (P2Pdevice d : Globals.p2pDevices) {
			if (d.equals(newJID, newIP, newPort))
				return d;
		}
		P2Pdevice newPeer = new P2Pdevice(newJID, newIP, newPort);
		Globals.p2pDevices.add(newPeer);
		newPeer.checkLocal();
		return newPeer;
	}
	
	private void checkLocal(){
		if(this.ip.equals(Globals.ipAddress)){
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
			}
		}
	}

	public boolean equals(P2Pdevice node) {
		if (this.port != node.getPort()) {
			return false;
		}
		if (!this.ip.equals(node.getIP())) {
			return false;
		}
		if (!this.jid.equals(node.getJID())) {
			return false;
		}
		return true;
	}

	public boolean equals(String newJID, String newIP, int newPort) {
		if (this.port != newPort) {
			return false;
		}
		if (!this.ip.equals(newIP)) {
			return false;
		}
		if (!this.jid.equals(newJID)) {
			return false;
		}
		return true;
	}

	public String getIP() {
		if(this.usingLocalIP)
			return this.localIP;
		
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}

	public String getJID() {
		return this.jid;
	}
}
